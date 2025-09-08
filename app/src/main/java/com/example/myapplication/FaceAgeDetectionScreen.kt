package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors
import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint

@Composable
fun FaceAgeDetectionScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        hasCameraPermission = granted
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    // State: face bounding box
    var faceRects by remember { mutableStateOf<List<android.graphics.Rect>>(emptyList()) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (hasCameraPermission) {
            CameraFaceDetectionView(lifecycleOwner, faceRects = faceRects, onFacesDetected = { rects ->
                faceRects = rects
            })
        } else {
            Text("Camera permission required", color = Color.Red)
        }
    }
}

@Composable
fun CameraFaceDetectionView(lifecycleOwner: LifecycleOwner, faceRects: List<android.graphics.Rect>, onFacesDetected: (List<android.graphics.Rect>) -> Unit) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val faceDetectorOptions = FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .build()
                val faceDetector = FaceDetection.getClient(faceDetectorOptions)
                val analysisUseCase = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysisUseCase.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy: ImageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        faceDetector.process(image)
                            .addOnSuccessListener { faces ->
                                val rects = faces.map { it.boundingBox }
                                onFacesDetected(rects)
                                // Show Toast only when a face is detected and only on this screen
                                if (faces.isNotEmpty() && ctx is android.app.Activity && ctx.hasWindowFocus()) {
                                    Toast.makeText(ctx, "Face detected! (age: TODO)", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { onFacesDetected(emptyList()) }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        onFacesDetected(emptyList())
                        imageProxy.close()
                    }
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analysisUseCase
                    )
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
    // Overlay face bounding box
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        for (rect in faceRects) {
            // Map rect to Compose canvas coordinates (assuming 640x480 preview), fix left-right flip
            val scaleX = canvasWidth / 640f
            val scaleY = canvasHeight / 480f
            val left = (640 - rect.right) * scaleX
            val top = rect.top * scaleY
            val right = (640 - rect.left) * scaleX
            val bottom = rect.bottom * scaleY
            drawRect(
                color = ComposeColor.Red,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                style = Stroke(width = 4f)
            )
        }
    }
} 
package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.PointF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executors

@Composable
fun PoseCameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var poseData by remember { mutableStateOf<PoseDrawData?>(null) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var frameW by remember { mutableStateOf(0f) }
    var frameH by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pose Detection (Camera)")
        if (!hasPermission) {
            Text("Camera permission is required.")
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Grant Permission") }
            return@Column
        }

        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
                CameraPreviewWithAnalyzer(
                    useFrontCamera = useFrontCamera,
                    onPoseResult = { data ->
                        poseData = data
                        frameW = data.frameWidth
                        frameH = data.frameHeight
                    }
                )
                PoseOverlay(poseData = poseData, mirrorHorizontally = useFrontCamera)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { useFrontCamera = !useFrontCamera }) {
                Text(if (useFrontCamera) "Switch to Back Camera" else "Switch to Front Camera")
            }
            Button(onClick = { poseData = null }) { Text("Clear") }
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreviewWithAnalyzer(
    useFrontCamera: Boolean,
    onPoseResult: (PoseDrawData) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val options = remember {
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    }
    val detector = remember { PoseDetection.getClient(options) }

    DisposableEffect(useFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val analysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        val cameraSelector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

        val executor = Executors.newSingleThreadExecutor()
        analysis.setAnalyzer(executor) { imageProxy: ImageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                detector.process(image)
                    .addOnSuccessListener { pose ->
                        val points = mutableListOf<PointF>()
                        val landmarks = listOf(
                            PoseLandmark.NOSE,
                            PoseLandmark.LEFT_SHOULDER,
                            PoseLandmark.RIGHT_SHOULDER,
                            PoseLandmark.LEFT_ELBOW,
                            PoseLandmark.RIGHT_ELBOW,
                            PoseLandmark.LEFT_WRIST,
                            PoseLandmark.RIGHT_WRIST,
                            PoseLandmark.LEFT_HIP,
                            PoseLandmark.RIGHT_HIP,
                            PoseLandmark.LEFT_KNEE,
                            PoseLandmark.RIGHT_KNEE,
                            PoseLandmark.LEFT_ANKLE,
                            PoseLandmark.RIGHT_ANKLE
                        )
                        for (id in landmarks) {
                            val lm = pose.getPoseLandmark(id)
                            if (lm != null) points.add(PointF(lm.position.x, lm.position.y))
                        }
                        onPoseResult(
                            PoseDrawData(
                                points = points,
                                frameWidth = image.width.toFloat(),
                                frameHeight = image.height.toFloat()
                            )
                        )
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
        } catch (e: Exception) {
            // ignore for demo
        }

        onDispose {
            try {
                cameraProvider.unbindAll()
            } catch (_: Exception) {}
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun PoseOverlay(poseData: PoseDrawData?, mirrorHorizontally: Boolean) {
    if (poseData == null) return
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        // Use minimal scale to fit and add center offset (letterbox)
        val scale = kotlin.math.min(size.width / poseData.frameWidth, size.height / poseData.frameHeight)
        val contentWidth = poseData.frameWidth * scale
        val contentHeight = poseData.frameHeight * scale
        val xOffset = (size.width - contentWidth) / 2f
        val yOffset = (size.height - contentHeight) / 2f
        poseData.points.forEach { p ->
            val mappedX = if (mirrorHorizontally) (poseData.frameWidth - p.x) * scale else p.x * scale
            val x = xOffset + mappedX
            val y = yOffset + p.y * scale
            drawCircle(Color.Green, radius = 6f, center = Offset(x, y))
        }
    }
}

data class PoseDrawData(
    val points: List<PointF>,
    val frameWidth: Float,
    val frameHeight: Float
)

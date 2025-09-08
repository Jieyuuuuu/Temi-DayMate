package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCoachScreen(navController: NavController) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) engine?.language = Locale.US }
        tts = engine
        onDispose { engine?.stop(); engine?.shutdown() }
    }

    val exercises = listOf(
        GuideExercise(ExerciseType.ArmCurl, name = "Arm Curl", targetReps = 10),
        GuideExercise(ExerciseType.Squat, name = "Squat", targetReps = 10)
    )
    var selected by remember { mutableStateOf(exercises.first()) }

    // Use fixed Fit 3:2 aspect ratio
    val scheme = remember {
        CameraScheme(
            label = "Fit 3:2",
            containerAspect = 3f / 2f,
            camAspect = AspectRatio.RATIO_4_3,
            scaleType = PreviewView.ScaleType.FIT_CENTER,
            scaleMode = CameraScaleMode.FIT,
            implMode = ImplMode.COMPATIBLE,
            hAnchor = HAnchor.CENTER,
            vAnchor = VAnchor.CENTER
        )
    }

    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    LaunchedEffect(Unit) { if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    var useFrontCamera by remember { mutableStateOf(true) }
    var reps by remember { mutableStateOf(0) }
    var stage by remember { mutableStateOf("idle") }
    var guidance by remember { mutableStateOf("Ready") }
    var lastAngle by remember { mutableStateOf(0.0) }

    var landmarks by remember { mutableStateOf<Map<Int, PointF>>(emptyMap()) }
    var frameW by remember { mutableStateOf(0) }
    var frameH by remember { mutableStateOf(0) }
    var frameRotation by remember { mutableStateOf(0) }

    LaunchedEffect(selected, landmarks) {
        if (landmarks.isEmpty()) return@LaunchedEffect
        when (selected.type) {
            ExerciseType.ArmCurl -> {
                val shoulder = landmarks[PoseLandmark.LEFT_SHOULDER]
                val elbow = landmarks[PoseLandmark.LEFT_ELBOW]
                val wrist = landmarks[PoseLandmark.LEFT_WRIST]
                if (shoulder != null && elbow != null && wrist != null) {
                    val angle = angleAt(elbow, shoulder, wrist)
                    lastAngle = angle
                    val downThresh = 150.0
                    val upThresh = 60.0
                    if (stage == "idle" || stage == "down") {
                        guidance = "Lower your arm"
                        if (angle > downThresh) stage = "up"
                    } else if (stage == "up") {
                        guidance = "Curl up"
                        if (angle < upThresh) {
                            reps += 1
                            stage = "down"
                            tts?.speak("Good! $reps", TextToSpeech.QUEUE_FLUSH, null, "rep")
                        }
                    }
                } else guidance = "Move into view"
            }
            ExerciseType.Squat -> {
                val hip = landmarks[PoseLandmark.LEFT_HIP]
                val knee = landmarks[PoseLandmark.LEFT_KNEE]
                val ankle = landmarks[PoseLandmark.LEFT_ANKLE]
                if (hip != null && knee != null && ankle != null) {
                    val angle = angleAt(knee, hip, ankle)
                    lastAngle = angle
                    val downThresh = 80.0
                    val upThresh = 160.0
                    if (stage == "idle" || stage == "down") {
                        guidance = "Go down"
                        if (angle < downThresh) stage = "up"
                    } else if (stage == "up") {
                        guidance = "Stand up"
                        if (angle > upThresh) {
                            reps += 1
                            stage = "down"
                            tts?.speak("Nice! $reps", TextToSpeech.QUEUE_FLUSH, null, "rep")
                        }
                    }
                } else guidance = "Move into view"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Exercise Coach", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            exercises.forEach { ex ->
                FilterChip(selected = ex == selected, onClick = { selected = ex; reps = 0; stage = "idle"; guidance = "Ready" }, label = { Text(ex.name) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AssistChip(onClick = {}, label = { Text("Reps") })
            Text("$reps / ${selected.targetReps}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            Spacer(Modifier.width(12.dp))
            AssistChip(onClick = {}, label = { Text("Angle") })
            Text("${"%.0f".format(lastAngle)}°", fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
        Surface(color = Color(0xFFEEF7FF)) { Text(guidance, modifier = Modifier.fillMaxWidth().padding(12.dp), fontSize = 18.sp, color = Color(0xFF1565C0)) }

        if (!hasPermission) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth()) { Text("Grant Camera Permission") }
            return@Column
        }

        Card(elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(scheme.containerAspect), contentAlignment = Alignment.Center) {
                CameraWithPoseAnalyzer(useFrontCamera = useFrontCamera, scheme = scheme) { map, w, h, rotation ->
                    landmarks = map
                    frameW = w
                    frameH = h
                    frameRotation = rotation
                }
                val fw = if (frameW > 0) frameW.toFloat() else 640f
                val fh = if (frameH > 0) frameH.toFloat() else 480f
                PoseSkeletonOverlayWithScaleMode(
                    pointsById = landmarks,
                    frameWidth = fw,
                    frameHeight = fh,
                    mirror = useFrontCamera,
                    scaleMode = scheme.scaleMode,
                    hAnchor = scheme.hAnchor,
                    vAnchor = scheme.vAnchor,
                    rotationDegrees = frameRotation
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { useFrontCamera = !useFrontCamera }, modifier = Modifier.weight(1f)) { Text(if (useFrontCamera) "Back Camera" else "Front Camera") }
            OutlinedButton(onClick = { reps = 0; stage = "idle"; guidance = "Ready" }, modifier = Modifier.weight(1f)) { Text("Reset") }
        }
        Text("Tip: Ensure a safe area, keep device stable, and move within the frame.", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun CameraWithPoseAnalyzer(
    useFrontCamera: Boolean,
    scheme: CameraScheme,
    onPose: (Map<Int, PointF>, Int, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = scheme.scaleType
            implementationMode = if (scheme.implMode == ImplMode.PERFORMANCE) PreviewView.ImplementationMode.PERFORMANCE else PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val options = remember { PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.STREAM_MODE).build() }
    val detector = remember { PoseDetection.getClient(options) }

    DisposableEffect(useFrontCamera, scheme) {
        val cameraProvider = cameraProviderFuture.get()
        previewView.scaleType = scheme.scaleType
        previewView.implementationMode = if (scheme.implMode == ImplMode.PERFORMANCE) PreviewView.ImplementationMode.PERFORMANCE else PreviewView.ImplementationMode.COMPATIBLE
        val preview = Preview.Builder().setTargetAspectRatio(scheme.camAspect).build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val analysis = ImageAnalysis.Builder().setTargetAspectRatio(scheme.camAspect).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        val cameraSelector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        val executor = Executors.newSingleThreadExecutor()
        analysis.setAnalyzer(executor) { imageProxy: ImageProxy ->
            val media = imageProxy.image
            if (media != null) {
                val rotation = imageProxy.imageInfo.rotationDegrees
                val image = InputImage.fromMediaImage(media, rotation)
                detector.process(image)
                    .addOnSuccessListener { pose ->
                        val ids = listOf(
                            PoseLandmark.NOSE, PoseLandmark.LEFT_EYE, PoseLandmark.RIGHT_EYE, PoseLandmark.LEFT_EAR, PoseLandmark.RIGHT_EAR,
                            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
                            PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
                            PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE, PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE
                        )
                        val map = buildMap<Int, PointF> { for (id in ids) pose.getPoseLandmark(id)?.let { put(id, PointF(it.position.x, it.position.y)) } }
                        onPose(map, image.width, image.height, rotation)
                    }
                    .addOnCompleteListener { imageProxy.close() }
            } else imageProxy.close()
        }
        try { cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis) } catch (_: Exception) {}
        onDispose { try { cameraProvider.unbindAll() } catch (_: Exception) {} }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
}

@Composable
private fun PoseSkeletonOverlayWithScaleMode(
    pointsById: Map<Int, PointF>,
    frameWidth: Float,
    frameHeight: Float,
    mirror: Boolean,
    scaleMode: CameraScaleMode,
    hAnchor: HAnchor,
    vAnchor: VAnchor,
    rotationDegrees: Int
) {
    if (pointsById.isEmpty()) return
    val connections = listOf(
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.NOSE to PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.NOSE to PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
        PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
        PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
        PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
        PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE
    )

    val (effectiveW, effectiveH, rotatedPoints) = remember(pointsById, frameWidth, frameHeight, rotationDegrees) {
        val list = pointsById.mapValues { (_, p) -> rotatePoint(p, frameWidth, frameHeight, rotationDegrees) }
        val effW = if (rotationDegrees % 180 == 0) frameWidth else frameHeight
        val effH = if (rotationDegrees % 180 == 0) frameHeight else frameWidth
        Triple(effW, effH, list)
    }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val sx = size.width / effectiveW
        val sy = size.height / effectiveH
        val scale = if (scaleMode == CameraScaleMode.FIT) min(sx, sy) else max(sx, sy)
        val contentWidth = effectiveW * scale
        val contentHeight = effectiveH * scale
        val xOffset = when (hAnchor) {
            HAnchor.START -> 0f
            HAnchor.CENTER -> (size.width - contentWidth) / 2f
            HAnchor.END -> size.width - contentWidth
        }
        val yOffset = when (vAnchor) {
            VAnchor.TOP -> 0f
            VAnchor.CENTER -> (size.height - contentHeight) / 2f
            VAnchor.BOTTOM -> size.height - contentHeight
        }
        fun map(p: PointF): Offset {
            val xMirror = if (mirror) (effectiveW - p.x) else p.x
            return Offset(xOffset + xMirror * scale, yOffset + p.y * scale)
        }
        connections.forEach { (a, b) ->
            val pa = rotatedPoints[a]
            val pb = rotatedPoints[b]
            if (pa != null && pb != null) drawLine(Color(0xFF4CAF50), start = map(pa), end = map(pb), strokeWidth = 6f)
        }
        rotatedPoints.values.forEach { p -> drawCircle(Color.Yellow, radius = 5f, center = map(p)) }
    }
}

private fun rotatePoint(p: PointF, w: Float, h: Float, rotation: Int): PointF {
    return when ((rotation % 360 + 360) % 360) {
        0 -> PointF(p.x, p.y)
        90 -> PointF(h - p.y, p.x)
        180 -> PointF(w - p.x, h - p.y)
        270 -> PointF(p.y, w - p.x)
        else -> PointF(p.x, p.y)
    }
}

private fun angleAt(center: PointF, a: PointF, c: PointF): Double {
    val v1x = a.x - center.x
    val v1y = a.y - center.y
    val v2x = c.x - center.x
    val v2y = c.y - center.y
    val dot = v1x * v2x + v1y * v2y
    val mag1 = sqrt(v1x.toDouble().pow(2) + v1y.toDouble().pow(2))
    val mag2 = sqrt(v2x.toDouble().pow(2) + v2y.toDouble().pow(2))
    if (mag1 == 0.0 || mag2 == 0.0) return 180.0
    val cos = (dot / (mag1 * mag2)).coerceIn(-1.0, 1.0)
    return Math.toDegrees(acos(cos))
}

data class GuideExercise(val type: ExerciseType, val name: String, val targetReps: Int)

enum class ExerciseType { ArmCurl, Squat }

enum class CameraScaleMode { FIT, FILL }

enum class ImplMode { COMPATIBLE, PERFORMANCE }

enum class HAnchor { START, CENTER, END }

enum class VAnchor { TOP, CENTER, BOTTOM }

data class CameraScheme(
    val label: String,
    val containerAspect: Float,
    val camAspect: Int,
    val scaleType: PreviewView.ScaleType,
    val scaleMode: CameraScaleMode,
    val implMode: ImplMode,
    val hAnchor: HAnchor,
    val vAnchor: VAnchor
)

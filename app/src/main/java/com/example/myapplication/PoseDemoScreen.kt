package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

@Composable
fun PoseDemoScreen(navController: NavController) {
    // Create a blank bitmap as a placeholder (replace with photo/camera frame in real use)
    val width = 640
    val height = 480
    var resultBitmap by remember { mutableStateOf(createBlankBitmap(width, height)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pose Detection Demo (Static)")
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Image(bitmap = resultBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(320.dp, 240.dp))
        }
        Button(onClick = {
            // Use current bitmap as input for demo; replace with camera/photo in production
            val image = InputImage.fromBitmap(resultBitmap, 0)
            val options = PoseDetectorOptions.Builder().setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE).build()
            val detector = PoseDetection.getClient(options)
            detector.process(image)
                .addOnSuccessListener { pose ->
                    val canvas = Canvas(resultBitmap)
                    val paint = Paint().apply {
                        color = Color.GREEN
                        strokeWidth = 8f
                    }
                    // Try to draw a few common joints
                    val nose = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.NOSE)
                    val leftShoulder = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.LEFT_SHOULDER)
                    val rightShoulder = pose.getPoseLandmark(com.google.mlkit.vision.pose.PoseLandmark.RIGHT_SHOULDER)
                    if (nose != null) canvas.drawCircle(nose.position.x, nose.position.y, 8f, paint)
                    if (leftShoulder != null) canvas.drawCircle(leftShoulder.position.x, leftShoulder.position.y, 8f, paint)
                    if (rightShoulder != null) canvas.drawCircle(rightShoulder.position.x, rightShoulder.position.y, 8f, paint)
                }
                .addOnFailureListener {
                    // Reset to blank bitmap on failure
                    resultBitmap = createBlankBitmap(width, height)
                }
        }) {
            Text("Run Pose Detection")
        }
    }
}

private fun createBlankBitmap(w: Int, h: Int): Bitmap {
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(Color.BLACK)
    return bmp
}

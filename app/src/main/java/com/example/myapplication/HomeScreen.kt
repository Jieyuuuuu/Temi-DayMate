package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import com.example.myapplication.FeatureDestination
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.border
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val featureColors = listOf(
    Brush.linearGradient(listOf(Color(0xFFB2FEFA), Color(0xFF6DD5FA))), // My Schedule
    Brush.linearGradient(listOf(Color(0xFFFFE29F), Color(0xFFFFB6B9))), // Medication Reminder
    Brush.linearGradient(listOf(Color(0xFFB993D6), Color(0xFFB5FFFC))), // Food Record
    Brush.linearGradient(listOf(Color(0xFFFFD6E0), Color(0xFFC9FFBF))), // Exercise & Wall
    Brush.linearGradient(listOf(Color(0xFF6DD5FA), Color(0xFFB993D6))), // Social
    Brush.linearGradient(listOf(Color(0xFFFFF6B7), Color(0xFFFFE29F))), // Memory Game
    Brush.linearGradient(listOf(Color(0xFFB5FFFC), Color(0xFFFFB6B9))), // My Memories
    Brush.linearGradient(listOf(Color(0xFFC9FFBF), Color(0xFFB993D6))), // Sleep Tracking
    Brush.linearGradient(listOf(Color(0xFFFFB6B9), Color(0xFFFFD6E0))), // Settings/Caregiver
    Brush.linearGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))  // AI Assistant
)
private val featureEmojis = listOf(
    "📅", "💊", "🍽️", "🏃", "🗣️", "🧩", "🖼️", "😴", "⚙️", "🤖"
)

@Composable
fun HomeScreen(navController: NavController) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    
    // 實時更新時間
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            // 使用12小時制，更符合一般使用習慣
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            
            currentTime = timeFormat.format(now.time)
            currentDate = dateFormat.format(now.time)
            
            delay(1000) // 每秒更新一次
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFe0eafc),
                        Color(0xFFcfdef3),
                        Color(0xFFa1c4fd)
                    )
                )
            )
    ) {
        // 時間顯示 - 放在右上角，不干擾主要內容
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 24.dp)
        ) {
            TimeDisplay(
                currentTime = currentTime,
                currentDate = currentDate
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // iOS style avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .shadow(10.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦾",
                    fontSize = 48.sp,
                    color = Color(0xFF007AFF)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Hi! I'm DayMate, nice to meet you!",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A237E)
            )
        }
        // 恢復原本的 grid 位置
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.TopCenter)
                .padding(top = 180.dp), // 恢復原本的 top padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FeatureGrid(navController)
        }
    }
}

@Composable
fun TimeDisplay(currentTime: String, currentDate: String) {
    Card(
        modifier = Modifier
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 時間顯示 - 縮小字體
            Text(
                text = currentTime,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 日期顯示 - 縮小字體
            Text(
                text = currentDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5A5A89)
            )
        }
    }
}

@Composable
fun FeatureGrid(navController: NavController) {
    val features = FeatureDestination.allFeatures
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val columns = 3
    val buttonSize = if (isLandscape) 110.dp else 120.dp
    val gridHeight = if (isLandscape) 370.dp else 440.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        itemsIndexed(features) { index, feature ->
            FeatureButtonModern(
                feature = feature,
                brush = featureColors[index],
                emoji = featureEmojis[index],
                onClick = { navController.navigate(feature.route) },
                size = buttonSize
            )
        }
    }
}

@Composable
fun FeatureButtonModern(feature: FeatureDestination, brush: Brush, emoji: String, onClick: () -> Unit, size: Dp) {
    val pressed = remember { androidx.compose.runtime.mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed.value) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120), label = "buttonScale"
    )
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(36.dp), ambientColor = Color.Black.copy(alpha=0.10f), spotColor = Color.Black.copy(alpha=0.10f))
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .border(1.dp, Color(0x22007AFF), RoundedCornerShape(36.dp))
            .clickable(
                enabled = true,
                onClickLabel = feature.name,
                onClick = {
                    pressed.value = true
                    onClick()
                    pressed.value = false
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Emoji with circular background (iOS style)
            Box(
                modifier = Modifier
                    .size(size * 0.44f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = (size.value * 0.36).sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = feature.name,
                fontSize = 15.sp,
                color = Color(0xFF1A237E),
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
} 
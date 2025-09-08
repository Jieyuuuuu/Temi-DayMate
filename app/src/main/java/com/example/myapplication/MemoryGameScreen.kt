package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(navController: NavController) {
    var currentGameState by remember { mutableStateOf(GameState.PREVIEW) }
    var gameState by remember { mutableStateOf(GameStateData().initializeGame()) }
    var timeElapsed by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Sound effect functions
    fun playCorrectSound() {
        scope.launch {
            try {
                val sound = MediaPlayer()
                val correctUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                sound.setDataSource(context, correctUri)
                sound.prepare()
                sound.start()
                delay(1000)
                sound.release()
            } catch (e: Exception) {
                // Ignore sound errors
            }
        }
    }
    
    fun playWrongSound() {
        scope.launch {
            try {
                val sound = MediaPlayer()
                val wrongUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                sound.setDataSource(context, wrongUri)
                sound.prepare()
                sound.start()
                delay(500)
                sound.release()
            } catch (e: Exception) {
                // Ignore sound errors
            }
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeElapsed++
        }
    }
    
    // Handle preview phase
    LaunchedEffect(currentGameState) {
        if (currentGameState == GameState.PREVIEW) {
            delay(3000) // Show for 3 seconds
            currentGameState = GameState.PLAYING
            gameState = gameState.startPlaying()
        }
    }
    
    // Handle wrong card delay flip back
    LaunchedEffect(gameState.lastWrongCard) {
        gameState.lastWrongCard?.let { wrongCard ->
            delay(1000) // Show wrong card for 1 second
            gameState = gameState.hideWrongCard()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Game") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (currentGameState) {
            GameState.PREVIEW -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Text(
                        text = "Remember these images!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp), // Less outer margin
                        horizontalArrangement = Arrangement.spacedBy(4.dp), // Less horizontal spacing
                        verticalArrangement = Arrangement.spacedBy(4.dp),   // Less vertical spacing
                        modifier = Modifier.weight(1f)
                    ) {
                        items(gameState.cards) { card ->
                            GameCard(
                                card = card,
                                onClick = { },
                                showImage = true
                            )
                        }
                    }
                }
            }
            GameState.PLAYING -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Game info row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Time: ${timeElapsed}s")
                        }
                        
                        Text("Score: $score")
                        
                        IconButton(onClick = { 
                            gameState = GameStateData().initializeGame()
                            timeElapsed = 0
                            score = 0
                            currentGameState = GameState.PREVIEW
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart")
                        }
                    }
                    
                    // Prompt text
                    gameState.currentTarget?.let { target ->
                        Text(
                            text = "Please select: $target",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    // Game cards grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp), // Less outer margin
                        horizontalArrangement = Arrangement.spacedBy(4.dp), // Less horizontal spacing
                        verticalArrangement = Arrangement.spacedBy(4.dp),   // Less vertical spacing
                        modifier = Modifier.weight(1f)
                    ) {
                        items(gameState.cards) { card ->
                            GameCard(
                                card = card,
                                onClick = {
                                    scope.launch {
                                        gameState = gameState.onCardClick(card.id)
                                        if (gameState.isCorrect) {
                                            // Play correct sound
                                            playCorrectSound()
                                            score += 10
                                            if (gameState.isGameComplete()) {
                                                currentGameState = GameState.COMPLETE
                                            }
                                        } else {
                                            // Play wrong sound
                                            playWrongSound()
                                        }
                                    }
                                },
                                showImage = card.isFlipped || card.isMatched
                            )
                        }
                    }
                }
            }
            GameState.COMPLETE -> {
                GameCompleteScreen(
                    score = score,
                    timeElapsed = timeElapsed,
                    onPlayAgain = {
                        gameState = GameStateData().initializeGame()
                        timeElapsed = 0
                        score = 0
                        currentGameState = GameState.PREVIEW
                    },
                    onBackToHome = {
                        navController.navigateUp()
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
    
    // Clean up sound resources
    DisposableEffect(Unit) {
        onDispose {
            // No explicit release needed here as MediaPlayer is released in the lambda
        }
    }
}

@Composable
fun GameCard(
    card: GameCard,
    onClick: () -> Unit,
    showImage: Boolean
) {
    Card(
        modifier = Modifier
            .aspectRatio(1.5f) // Wider ratio to make cards smaller
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (showImage) {
                // Show image
                Image(
                    painter = painterResource(id = card.imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp), // Minimal padding
                    contentScale = ContentScale.Fit
                )
            } else {
                // Show back side
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = Color.White,
                        fontSize = 14.sp, // Smaller font
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GameCompleteScreen(
    score: Int,
    timeElapsed: Int,
    onPlayAgain: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Congratulations!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Score: $score",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "Time: ${timeElapsed}s",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onPlayAgain,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Play Again")
        }
        
        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Back to Home")
        }
    }
}

// Data classes
data class GameCard(
    val id: Int,
    val imageResId: Int,
    val imageName: String,
    val isMatched: Boolean = false,
    val isFlipped: Boolean = false
)

data class GameStateData(
    val cards: List<GameCard> = emptyList(),
    val currentTarget: String? = null,
    val remainingTargets: List<String> = emptyList(),
    val isCorrect: Boolean = false,
    val lastWrongCard: Int? = null
) {
    fun initializeGame(): GameStateData {
        // Select 6 images from gallery
        val availableImages = listOf(
            Triple(R.drawable.book, "book", "Book"),
            Triple(R.drawable.apple, "apple", "Apple"),
            Triple(R.drawable.banana, "banana", "Banana"),
            Triple(R.drawable.car, "car", "Car"),
            Triple(R.drawable.cat, "cat", "Cat"),
            Triple(R.drawable.bus, "bus", "Bus"),
            Triple(R.drawable.pen, "pen", "Pen"),
            Triple(R.drawable.phone, "phone", "Phone"),
            Triple(R.drawable.flower, "flower", "Flower")
        )
        
        // Randomly select 6 images
        val selectedImages = availableImages.shuffled().take(6)
        
        // Create cards
        val gameCards = selectedImages.mapIndexed { index: Int, triple: Triple<Int, String, String> ->
            GameCard(id = index, imageResId = triple.first, imageName = triple.second)
        }
        
        return copy(
            cards = gameCards,
            remainingTargets = selectedImages.map { it.second }.shuffled()
        )
    }
    
    fun startPlaying(): GameStateData {
        return copy(
            currentTarget = remainingTargets.firstOrNull()
        )
    }
    
    fun onCardClick(cardId: Int): GameStateData {
        val card = cards.find { it.id == cardId } ?: return this
        val target = currentTarget ?: return this
        
        return if (card.imageName == target) {
            // Correct selection
            val newCards = cards.map { 
                if (it.id == cardId) it.copy(isMatched = true, isFlipped = true)
                else it
            }
            val newRemainingTargets = remainingTargets.drop(1)
            val newCurrentTarget = newRemainingTargets.firstOrNull()
            
            copy(
                cards = newCards,
                currentTarget = newCurrentTarget,
                remainingTargets = newRemainingTargets,
                isCorrect = true,
                lastWrongCard = null
            )
        } else {
            // Wrong selection
            val newCards = cards.map { 
                if (it.id == cardId) it.copy(isFlipped = true)
                else it
            }
            
            copy(
                cards = newCards,
                isCorrect = false,
                lastWrongCard = cardId
            )
        }
    }
    
    fun hideWrongCard(): GameStateData {
        val newCards = cards.map { 
            if (it.id == lastWrongCard) it.copy(isFlipped = false)
            else it
        }
        
        return copy(
            cards = newCards,
            lastWrongCard = null
        )
    }
    
    fun isGameComplete(): Boolean {
        return remainingTargets.isEmpty()
    }
}

enum class GameState {
    PREVIEW,
    PLAYING,
    COMPLETE
} 
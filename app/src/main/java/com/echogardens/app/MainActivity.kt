package com.echogardens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.echogardens.app.audio.HapticManager
import com.echogardens.app.engine.AuraEngine
import com.echogardens.app.engine.FocusCategory
import com.echogardens.app.engine.IslandState
import com.echogardens.app.focus.FocusViewModel
import com.echogardens.app.focus.SessionResult
import com.echogardens.app.ui.screens.FocusScreen
import com.echogardens.app.ui.screens.HomeScreen
import com.echogardens.app.ui.screens.SettingsScreen
import com.echogardens.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Echo Gardens - A Living Digital Ecosystem
 * 
 * Main Activity that hosts the Compose navigation and core game logic.
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var auraEngine: AuraEngine
    private lateinit var hapticManager: HapticManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize core systems
        auraEngine = AuraEngine(applicationContext)
        hapticManager = HapticManager(applicationContext)
        
        setContent {
            EchoGardensTheme(darkTheme = true) {
                EchoGardensApp(
                    auraEngine = auraEngine,
                    hapticManager = hapticManager
                )
            }
        }
    }
}

@Composable
fun EchoGardensApp(
    auraEngine: AuraEngine,
    hapticManager: HapticManager
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    
    // Collect states
    val islandState by auraEngine.islandState.collectAsState(initial = IslandState())
    val hapticsEnabled by auraEngine.hapticsEnabled.collectAsState(initial = true)
    val soundEnabled by auraEngine.soundEnabled.collectAsState(initial = true)
    
    // Create ViewModel (in real app, use ViewModelProvider)
    val focusViewModel = remember { FocusViewModel(auraEngine) }
    
    // Session complete dialog state
    var sessionResult by remember { mutableStateOf<SessionResult?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    islandState = islandState,
                    onStartFocus = { navController.navigate("focus") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            
            composable("focus") {
                FocusScreen(
                    viewModel = focusViewModel,
                    onSessionComplete = { result ->
                        sessionResult = result
                        // Haptic feedback on completion
                        if (hapticsEnabled) {
                            hapticManager.vibrateSuccess()
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    hapticsEnabled = hapticsEnabled,
                    soundEnabled = soundEnabled,
                    onHapticsToggle = { enabled ->
                        scope.launch { auraEngine.setHapticsEnabled(enabled) }
                        if (enabled) hapticManager.vibrateTick()
                    },
                    onSoundToggle = { enabled ->
                        scope.launch { auraEngine.setSoundEnabled(enabled) }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        // Session complete dialog
        sessionResult?.let { result ->
            SessionCompleteDialog(
                result = result,
                onDismiss = { sessionResult = null }
            )
        }
    }
}

@Composable
fun SessionCompleteDialog(
    result: SessionResult,
    onDismiss: () -> Unit
) {
    // Animation
    val infiniteTransition = rememberInfiniteTransition(label = "star")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )
    
    val categoryName = when (result.category) {
        FocusCategory.WORK -> "Work"
        FocusCategory.HEALTH -> "Health"
        FocusCategory.LEARNING -> "Learning"
        FocusCategory.SOCIAL -> "Social"
    }
    
    val categoryColor = when (result.category) {
        FocusCategory.WORK -> BranchWork
        FocusCategory.HEALTH -> BranchHealth
        FocusCategory.LEARNING -> BranchLearning
        FocusCategory.SOCIAL -> BranchSocial
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stardust icon
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Stardust",
                    tint = Stardust,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(starScale)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Session Complete!",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${result.durationMinutes} minutes of $categoryName",
                    color = categoryColor,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Reward display
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Stardust.copy(alpha = 0.2f),
                                    StardustGlow.copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${result.stardustEarned}",
                        color = Stardust,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Stardust,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Your tree is growing stronger! 🌳",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Continue",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

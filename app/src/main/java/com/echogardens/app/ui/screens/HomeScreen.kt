package com.echogardens.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echogardens.app.engine.AuraPhase
import com.echogardens.app.engine.IslandState
import com.echogardens.app.ui.components.SceneViewIsland
import com.echogardens.app.ui.theme.*

/**
 * Home Screen - The main island view.
 * Shows the player's island, World Tree, stardust count, and quick actions.
 */
@Composable
fun HomeScreen(
    islandState: IslandState,
    onStartFocus: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for the start button
    val infiniteTransition = rememberInfiniteTransition(label = "button_pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 3D Island scene as background
        SceneViewIsland(
            islandState = islandState,
            modifier = Modifier.fillMaxSize()
        )
        
        // Top bar with stardust and settings
        TopBar(
            stardust = islandState.stardust,
            auraPhase = islandState.auraPhase,
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        )
        
        // Bottom action area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phase indicator
            PhaseIndicator(auraPhase = islandState.auraPhase)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start Focus button
            FloatingActionButton(
                onClick = onStartFocus,
                modifier = Modifier
                    .size(72.dp)
                    .scale(buttonScale)
                    .shadow(12.dp, CircleShape),
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Focus",
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start Focus",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TopBar(
    stardust: Long,
    auraPhase: AuraPhase,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stardust counter
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Stardust",
                tint = Stardust,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$stardust",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        // Settings button
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PhaseIndicator(auraPhase: AuraPhase) {
    val text = if (auraPhase == AuraPhase.BLOOM) "✨ Bloom Phase" else "🌫️ Mist Phase"
    val colors = if (auraPhase == AuraPhase.BLOOM) {
        listOf(BloomAccent, BloomGlow)
    } else {
        listOf(MistAccent, MistFog)
    }
    
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(colors.map { it.copy(alpha = 0.8f) })
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        color = Color.Black.copy(alpha = 0.8f),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
}

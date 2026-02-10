package com.echogardens.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echogardens.app.engine.FocusCategory
import com.echogardens.app.focus.FocusViewModel
import com.echogardens.app.focus.SessionResult
import com.echogardens.app.ui.theme.*

/**
 * Focus Screen - Where users start and manage focus sessions.
 * Beautiful timer UI with category selection.
 */
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onSessionComplete: (SessionResult) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val remainingMillis by viewModel.remainingMillis.collectAsState()
    
    // Listen for session completion
    LaunchedEffect(Unit) {
        viewModel.sessionCompleted.collect { result ->
            onSessionComplete(result)
        }
    }
    
    // Session selection state
    var selectedCategory by remember { mutableStateOf<FocusCategory?>(null) }
    var selectedDuration by remember { mutableStateOf<Int?>(null) }
    
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        PrimaryDark.copy(alpha = 0.3f),
                        SurfaceDark
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    if (isRunning) viewModel.stopSession()
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (!isRunning) {
                // Category selection
                Text(
                    text = "What would you like to focus on?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                CategoryGrid(
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Duration selection
                AnimatedVisibility(
                    visible = selectedCategory != null,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "How long?",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        DurationGrid(
                            durations = viewModel.availableDurations,
                            selectedDuration = selectedDuration,
                            onDurationSelect = { selectedDuration = it }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Start button
                AnimatedVisibility(
                    visible = selectedCategory != null && selectedDuration != null,
                    enter = fadeIn() + scaleIn()
                ) {
                    Button(
                        onClick = {
                            selectedCategory?.let { cat ->
                                selectedDuration?.let { dur ->
                                    viewModel.startSession(cat, dur)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Begin Focus", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Active session UI
                ActiveSessionUI(
                    remainingMillis = remainingMillis,
                    isPaused = isPaused,
                    category = selectedCategory ?: FocusCategory.WORK,
                    formatTime = viewModel::formatTime,
                    onPause = viewModel::pauseSession,
                    onResume = viewModel::resumeSession,
                    onStop = {
                        viewModel.stopSession()
                        onBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    selectedCategory: FocusCategory?,
    onCategorySelect: (FocusCategory) -> Unit
) {
    val categories = listOf(
        CategoryItem(FocusCategory.WORK, "Work", Icons.Default.Work, BranchWork),
        CategoryItem(FocusCategory.HEALTH, "Health", Icons.Default.Favorite, BranchHealth),
        CategoryItem(FocusCategory.LEARNING, "Learning", Icons.Default.School, BranchLearning),
        CategoryItem(FocusCategory.SOCIAL, "Social", Icons.Default.Groups, BranchSocial)
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        items(categories) { item ->
            CategoryCard(
                item = item,
                isSelected = selectedCategory == item.category,
                onClick = { onCategorySelect(item.category) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    item: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) item.color.copy(alpha = 0.9f) else item.color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = if (isSelected) Color.White else item.color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DurationGrid(
    durations: List<Int>,
    selectedDuration: Int?,
    onDurationSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        durations.forEach { duration ->
            DurationChip(
                minutes = duration,
                isSelected = selectedDuration == duration,
                onClick = { onDurationSelect(duration) }
            )
        }
    }
}

@Composable
private fun DurationChip(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(),
        label = "scale"
    )
    
    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable(onClick = onClick),
        color = if (isSelected) Primary else Color.White.copy(alpha = 0.1f),
        shape = CircleShape
    ) {
        Text(
            text = "${minutes}m",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ActiveSessionUI(
    remainingMillis: Long,
    isPaused: Boolean,
    category: FocusCategory,
    formatTime: (Long) -> String,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    
    val categoryColor = when (category) {
        FocusCategory.WORK -> BranchWork
        FocusCategory.HEALTH -> BranchHealth
        FocusCategory.LEARNING -> BranchLearning
        FocusCategory.SOCIAL -> BranchSocial
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Timer circle
        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(if (!isPaused) breathScale else 1f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            categoryColor.copy(alpha = 0.6f),
                            categoryColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(remainingMillis),
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light
                )
                if (isPaused) {
                    Text(
                        text = "Paused",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Stop button
            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Pause/Resume button
            FloatingActionButton(
                onClick = { if (isPaused) onResume() else onPause() },
                modifier = Modifier.size(72.dp),
                containerColor = categoryColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Motivational text
        Text(
            text = if (isPaused) "Take your time" else "Growing your tree...",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

private data class CategoryItem(
    val category: FocusCategory,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

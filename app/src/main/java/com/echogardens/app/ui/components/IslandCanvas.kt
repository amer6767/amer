package com.echogardens.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import com.echogardens.app.engine.AuraPhase
import com.echogardens.app.engine.FocusCategory
import com.echogardens.app.engine.IslandState
import com.echogardens.app.ui.theme.*
import kotlin.math.*

/**
 * The Island Canvas renders the player's island with the World Tree.
 * Visual style changes based on Aura phase (Bloom vs Mist).
 */
@Composable
fun IslandCanvas(
    islandState: IslandState,
    modifier: Modifier = Modifier
) {
    // Animate phase transition
    val phaseTransition by animateFloatAsState(
        targetValue = if (islandState.auraPhase == AuraPhase.BLOOM) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = EaseInOutCubic),
        label = "phase_transition"
    )
    
    // Subtle breathing animation for the tree
    val infiniteTransition = rememberInfiniteTransition(label = "tree_breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    
    // Glow pulse animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Star twinkle for stardust display
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Draw sky gradient based on phase
        drawSkyGradient(phaseTransition)
        
        // Draw island base
        drawIsland(centerX, centerY * 1.3f, phaseTransition)
        
        // Draw World Tree
        drawWorldTree(
            centerX = centerX,
            baseY = centerY * 1.1f,
            treeLevel = islandState.worldTreeLevel,
            categoryProgress = islandState.categoryProgress,
            breathScale = breathScale,
            glowAlpha = glowAlpha,
            phaseTransition = phaseTransition
        )
        
        // Draw floating particles (stardust ambient)
        if (islandState.auraPhase == AuraPhase.BLOOM) {
            drawFloatingParticles(starTwinkle)
        } else {
            drawMistParticles(glowAlpha)
        }
    }
}

private fun DrawScope.drawSkyGradient(phaseTransition: Float) {
    val bloomBrush = Brush.verticalGradient(
        colors = listOf(BloomSky, BloomGradientEnd)
    )
    val mistBrush = Brush.verticalGradient(
        colors = listOf(MistGradientStart, MistGradientEnd)
    )
    
    // Fade between brushes
    drawRect(brush = mistBrush)
    drawRect(brush = bloomBrush, alpha = phaseTransition)
}

private fun DrawScope.drawIsland(centerX: Float, baseY: Float, phaseTransition: Float) {
    val islandWidth = size.width * 0.85f
    val islandHeight = size.height * 0.25f
    
    // Island color based on phase
    val grassColor = lerp(MistGrass, BloomGrass, phaseTransition)
    
    // Draw elliptical island
    val path = Path().apply {
        moveTo(centerX - islandWidth / 2, baseY)
        cubicTo(
            centerX - islandWidth / 2, baseY - islandHeight * 0.5f,
            centerX - islandWidth / 4, baseY - islandHeight,
            centerX, baseY - islandHeight * 0.8f
        )
        cubicTo(
            centerX + islandWidth / 4, baseY - islandHeight,
            centerX + islandWidth / 2, baseY - islandHeight * 0.5f,
            centerX + islandWidth / 2, baseY
        )
        cubicTo(
            centerX + islandWidth / 2, baseY + islandHeight * 0.3f,
            centerX, baseY + islandHeight * 0.5f,
            centerX - islandWidth / 2, baseY
        )
        close()
    }
    
    // Shadow
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.2f),
        style = Fill
    )
    
    // Main island
    translate(top = -20f) {
        drawPath(
            path = path,
            color = grassColor,
            style = Fill
        )
    }
}

private fun DrawScope.drawWorldTree(
    centerX: Float,
    baseY: Float,
    treeLevel: Int,
    categoryProgress: Map<FocusCategory, Float>,
    breathScale: Float,
    glowAlpha: Float,
    phaseTransition: Float
) {
    val treeHeight = size.height * 0.35f * (0.7f + (treeLevel / 10f) * 0.3f)
    val trunkWidth = 25f + treeLevel * 3f
    
    // Draw glow behind tree
    val glowColor = lerp(MistAccent, TreeGlow, phaseTransition)
    drawCircle(
        color = glowColor.copy(alpha = glowAlpha * 0.5f),
        radius = treeHeight * 0.8f * breathScale,
        center = Offset(centerX, baseY - treeHeight * 0.5f)
    )
    
    // Draw trunk
    val trunkPath = Path().apply {
        moveTo(centerX - trunkWidth / 2, baseY)
        lineTo(centerX - trunkWidth / 3, baseY - treeHeight * 0.4f)
        lineTo(centerX - trunkWidth / 4, baseY - treeHeight * 0.7f)
        lineTo(centerX, baseY - treeHeight)
        lineTo(centerX + trunkWidth / 4, baseY - treeHeight * 0.7f)
        lineTo(centerX + trunkWidth / 3, baseY - treeHeight * 0.4f)
        lineTo(centerX + trunkWidth / 2, baseY)
        close()
    }
    
    drawPath(
        path = trunkPath,
        color = TreeTrunk,
        style = Fill
    )
    
    // Draw branches for each category
    val branchLength = treeHeight * 0.3f * breathScale
    val branchStartY = baseY - treeHeight * 0.6f
    
    // Work branch (right-up)
    drawBranch(
        startX = centerX,
        startY = branchStartY,
        angle = -30f,
        length = branchLength * (0.5f + categoryProgress[FocusCategory.WORK]!! * 0.5f),
        color = BranchWork,
        glowAlpha = glowAlpha
    )
    
    // Health branch (left-up)
    drawBranch(
        startX = centerX,
        startY = branchStartY,
        angle = -150f,
        length = branchLength * (0.5f + categoryProgress[FocusCategory.HEALTH]!! * 0.5f),
        color = BranchHealth,
        glowAlpha = glowAlpha
    )
    
    // Learning branch (right-side)
    drawBranch(
        startX = centerX,
        startY = branchStartY + treeHeight * 0.15f,
        angle = -45f,
        length = branchLength * 0.8f * (0.5f + categoryProgress[FocusCategory.LEARNING]!! * 0.5f),
        color = BranchLearning,
        glowAlpha = glowAlpha
    )
    
    // Social branch (left-side)
    drawBranch(
        startX = centerX,
        startY = branchStartY + treeHeight * 0.15f,
        angle = -135f,
        length = branchLength * 0.8f * (0.5f + categoryProgress[FocusCategory.SOCIAL]!! * 0.5f),
        color = BranchSocial,
        glowAlpha = glowAlpha
    )
    
    // Draw canopy/leaves
    val leafRadius = treeHeight * 0.25f * breathScale
    val leafColor = lerp(MistGrass, TreeLeaves, phaseTransition)
    
    // Multiple overlapping circles for organic canopy
    listOf(
        Offset(centerX, baseY - treeHeight * 0.85f) to leafRadius,
        Offset(centerX - leafRadius * 0.6f, baseY - treeHeight * 0.75f) to leafRadius * 0.8f,
        Offset(centerX + leafRadius * 0.6f, baseY - treeHeight * 0.75f) to leafRadius * 0.8f,
        Offset(centerX, baseY - treeHeight * 0.65f) to leafRadius * 0.6f
    ).forEach { (offset, radius) ->
        drawCircle(
            color = leafColor,
            radius = radius,
            center = offset
        )
    }
}

private fun DrawScope.drawBranch(
    startX: Float,
    startY: Float,
    angle: Float,
    length: Float,
    color: Color,
    glowAlpha: Float
) {
    val radians = Math.toRadians(angle.toDouble())
    val endX = startX + (cos(radians) * length).toFloat()
    val endY = startY + (sin(radians) * length).toFloat()
    
    // Glow
    drawLine(
        color = color.copy(alpha = glowAlpha * 0.5f),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 16f,
        cap = StrokeCap.Round
    )
    
    // Branch
    drawLine(
        color = color,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
    
    // Leaf orb at end
    drawCircle(
        color = color,
        radius = 12f,
        center = Offset(endX, endY)
    )
}

private fun DrawScope.drawFloatingParticles(twinkle: Float) {
    val random = kotlin.random.Random(42) // Fixed seed for consistent positions
    repeat(20) { i ->
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height * 0.6f
        val alpha = (random.nextFloat() * 0.5f + 0.3f) * twinkle
        val radius = random.nextFloat() * 4f + 2f
        
        drawCircle(
            color = Stardust.copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawMistParticles(alpha: Float) {
    val random = kotlin.random.Random(42)
    repeat(10) { i ->
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height * 0.5f
        val radius = 30f + random.nextFloat() * 50f
        
        drawCircle(
            color = MistFog.copy(alpha = alpha * 0.3f),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

// Helper function to lerp between colors
private fun lerp(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}

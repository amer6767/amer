package com.echogardens.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.echogardens.app.engine.AuraPhase
import com.echogardens.app.engine.IslandState
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode

/**
 * 3D Island scene that displays ground models loaded from GLB files
 * and a simple test creature. Replaces the old 2D IslandCanvas.
 *
 * Uses SceneView (Filament) for real-time 3D rendering in Compose.
 */
@Composable
fun SceneViewIsland(
    islandState: IslandState,
    modifier: Modifier = Modifier
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    // Center node that everything orbits around
    val centerNode = rememberNode(engine)

    // Camera positioned above and to the side, looking down at the ground
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0.0f, y = 2.5f, z = 4.0f)
        lookAt(centerNode)
    }

    // Slow rotation animation for the camera to orbit the scene
    val infiniteTransition = rememberInfiniteTransition(label = "SceneRotation")
    val rotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing)
        ),
        label = "camera_orbit"
    )

    // Creature bobbing animation
    val creatureBob by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "creature_bob"
    )

    // Ground model node
    val groundNode = rememberNode {
        ModelNode(
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/grass_ground.glb"
            ),
            scaleToUnits = 2.0f
        ).apply {
            position = Position(x = 0f, y = 0f, z = 0f)
        }
    }

    // Second ground tile for variety
    val groundNode2 = rememberNode {
        ModelNode(
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/grass_tile.glb"
            ),
            scaleToUnits = 1.5f
        ).apply {
            position = Position(x = 1.8f, y = 0f, z = -0.5f)
        }
    }

    // Test creature - use the terrain model scaled down as a "creature"
    // (just to test that multiple models load and the game works)
    val creatureNode = rememberNode {
        ModelNode(
            modelInstance = modelLoader.createModelInstance(
                assetFileLocation = "models/grass_terrain.glb"
            ),
            scaleToUnits = 0.3f
        ).apply {
            position = Position(x = 0.5f, y = creatureBob, z = 0.5f)
        }
    }

    // Build the child nodes list
    val childNodes = remember(groundNode, groundNode2, creatureNode, centerNode) {
        listOf(centerNode, groundNode, groundNode2, creatureNode)
    }

    // Update creature position every frame for bobbing effect
    Scene(
        modifier = modifier,
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        cameraManipulator = rememberCameraManipulator(
            orbitHomePosition = cameraNode.worldPosition,
            targetPosition = centerNode.worldPosition
        ),
        childNodes = childNodes,
        onFrame = {
            // Slowly rotate the center node so the camera orbits the scene
            centerNode.rotation = Rotation(y = rotationY)

            // Bob the creature up and down
            creatureNode.position = Position(
                x = 0.5f,
                y = creatureBob,
                z = 0.5f
            )

            // Make creature spin
            creatureNode.rotation = Rotation(y = rotationY * 3f)

            // Keep camera looking at center
            cameraNode.lookAt(centerNode)
        }
    )
}

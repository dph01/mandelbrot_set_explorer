package mandelbrot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MandelbrotCanvas(state: MandelbrotState, modifier: Modifier = Modifier) {
    var canvasSize by remember { mutableStateOf(IntSize(800, 600)) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var renderJob by remember { mutableStateOf<Job?>(null) }

    // Function to launch a render, cancelling any in-flight render
    fun requestRender() {
        renderJob?.cancel()
        renderJob = coroutineScope.launch {
            val size = canvasSize
            if (size.width <= 0 || size.height <= 0) return@launch
            val cx = state.centerX
            val cy = state.centerY
            val z = state.zoom
            val scale = if (state.isZooming) 2 else 1
            val w = size.width / scale
            val h = size.height / scale

            val bitmap = withContext(Dispatchers.Default) {
                MandelbrotRenderer.render(
                    width = w,
                    height = h,
                    centerX = cx,
                    centerY = cy,
                    zoom = z
                )
            }
            imageBitmap = bitmap
        }
    }

    // Initial render
    LaunchedEffect(Unit) {
        requestRender()
    }

    // Re-render on canvas resize
    LaunchedEffect(canvasSize) {
        requestRender()
    }

    // Render full-res when zoom stops
    LaunchedEffect(state.isZooming) {
        if (!state.isZooming) {
            requestRender()
        }
    }

    // Zoom animation loop — updates state continuously, independent of render speed
    LaunchedEffect(state.isZooming, state.targetX, state.targetY, state.zoomSpeed) {
        if (!state.isZooming) return@LaunchedEffect

        var lastTime = System.nanoTime()

        while (state.isZooming) {
            kotlinx.coroutines.delay(16)

            val now = System.nanoTime()
            val dt = (now - lastTime) / 1_000_000_000.0
            lastTime = now

            val zoomFactor = 1.0 + state.zoomSpeed * dt
            state.zoom *= zoomFactor

            val easeRate = minOf(1.0, 2.0 * dt)
            state.centerX += (state.targetX - state.centerX) * easeRate
            state.centerY += (state.targetY - state.centerY) * easeRate

            requestRender()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val (re, im) = state.screenToComplex(
                            offset.x,
                            offset.y,
                            canvasSize.width,
                            canvasSize.height
                        )
                        state.setTarget(re, im)
                    }
                )
            }
    ) {
        imageBitmap?.let { bmp ->
            Image(
                bitmap = bmp,
                contentDescription = "Mandelbrot Set",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.Low
            )
        }
    }
}

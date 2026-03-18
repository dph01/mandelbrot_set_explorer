package mandelbrot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MandelbrotState {
    var centerX by mutableStateOf(-0.5)
    var centerY by mutableStateOf(0.0)
    var zoom by mutableStateOf(1.0)
    var zoomSpeed by mutableStateOf(0.037f) // 0.01..0.12 range
    var isZooming by mutableStateOf(false)
    var targetX by mutableStateOf(0.0)
    var targetY by mutableStateOf(0.0)

    companion object {
        const val DEFAULT_CENTER_X = -0.5
        const val DEFAULT_CENTER_Y = 0.0
        const val DEFAULT_ZOOM = 1.0
        const val MAX_ITERATIONS = 256
    }

    fun reset() {
        centerX = DEFAULT_CENTER_X
        centerY = DEFAULT_CENTER_Y
        zoom = DEFAULT_ZOOM
        isZooming = false
    }

    fun setTarget(cx: Double, cy: Double) {
        targetX = cx
        targetY = cy
        isZooming = true
    }

    fun screenToComplex(screenX: Float, screenY: Float, width: Int, height: Int): Pair<Double, Double> {
        val scale = 3.0 / (zoom * minOf(width, height))
        val re = centerX + (screenX - width / 2.0) * scale
        val im = centerY + (screenY - height / 2.0) * scale
        return Pair(re, im)
    }
}

package mandelbrot

import androidx.compose.ui.graphics.Color
import kotlin.math.ln
import kotlin.math.log2

object ColorMapper {

    fun map(iterations: Int, maxIterations: Int, zReal: Double, zImag: Double): Color {
        if (iterations >= maxIterations) return Color.Black

        // Smooth coloring: add fractional escape value
        val modulus = zReal * zReal + zImag * zImag
        val smoothed = iterations + 1 - ln(ln(modulus) / 2.0) / ln(2.0)

        // Map to hue with palette cycling
        val hue = ((smoothed * 7.0) % 360.0).toFloat()
        val saturation = 0.85f
        val brightness = 1.0f

        return hsbToColor(hue, saturation, brightness)
    }

    private fun hsbToColor(hue: Float, saturation: Float, brightness: Float): Color {
        val h = (hue % 360f + 360f) % 360f
        val c = brightness * saturation
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = brightness - c

        val (r, g, b) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(r + m, g + m, b + m)
    }
}

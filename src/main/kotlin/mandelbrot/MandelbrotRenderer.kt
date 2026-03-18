package mandelbrot

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.util.stream.IntStream
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log2

object MandelbrotRenderer {

    fun render(
        width: Int,
        height: Int,
        centerX: Double,
        centerY: Double,
        zoom: Double,
        maxIterations: Int = MandelbrotState.MAX_ITERATIONS
    ): ImageBitmap {
        val w = maxOf(width, 1)
        val h = maxOf(height, 1)
        val pixels = IntArray(w * h)
        val scale = 3.0 / (zoom * minOf(w, h))
        val halfW = w / 2.0
        val halfH = h / 2.0

        // Parallel row computation — each row is independent
        IntStream.range(0, h).parallel().forEach { py ->
            val cIm = centerY + (py - halfH) * scale
            for (px in 0 until w) {
                val cRe = centerX + (px - halfW) * scale

                var zRe = 0.0
                var zIm = 0.0
                var iteration = 0

                while (zRe * zRe + zIm * zIm <= 4.0 && iteration < maxIterations) {
                    val tmp = zRe * zRe - zIm * zIm + cRe
                    zIm = 2.0 * zRe * zIm + cIm
                    zRe = tmp
                    iteration++
                }

                // Inline color mapping to avoid per-pixel object allocation
                val rgb: Int
                if (iteration >= maxIterations) {
                    rgb = 0xFF000000.toInt()
                } else {
                    val modulus = zRe * zRe + zIm * zIm
                    val smoothed = iteration + 1.0 - ln(ln(modulus) / 2.0) / ln(2.0)
                    val hue = ((smoothed * 7.0) % 360.0 + 360.0) % 360.0
                    val sat = 0.85f
                    val bri = 1.0f

                    val h2 = hue.toFloat()
                    val c = bri * sat
                    val x = c * (1f - abs((h2 / 60f) % 2f - 1f))
                    val m = bri - c

                    val r: Float
                    val g: Float
                    val b: Float
                    when {
                        h2 < 60f -> { r = c + m; g = x + m; b = m }
                        h2 < 120f -> { r = x + m; g = c + m; b = m }
                        h2 < 180f -> { r = m; g = c + m; b = x + m }
                        h2 < 240f -> { r = m; g = x + m; b = c + m }
                        h2 < 300f -> { r = x + m; g = m; b = c + m }
                        else -> { r = c + m; g = m; b = x + m }
                    }

                    val ri = (r * 255).toInt().coerceIn(0, 255)
                    val gi = (g * 255).toInt().coerceIn(0, 255)
                    val bi = (b * 255).toInt().coerceIn(0, 255)
                    rgb = (0xFF shl 24) or (ri shl 16) or (gi shl 8) or bi
                }
                pixels[py * w + px] = rgb
            }
        }

        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(0, 0, w, h, pixels, 0, w)
        return image.toComposeImageBitmap()
    }
}

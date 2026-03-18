package mandelbrot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState(width = 1024.dp, height = 768.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Mandelbrot Set Explorer",
        state = windowState
    ) {
        val mandelbrotState = remember { MandelbrotState() }

        Column(modifier = Modifier.fillMaxSize()) {
            Controls(mandelbrotState)
            MandelbrotCanvas(
                state = mandelbrotState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

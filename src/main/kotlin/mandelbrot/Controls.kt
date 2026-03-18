package mandelbrot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Controls(state: MandelbrotState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A2E))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Zoom Speed:",
            color = Color.White,
            fontSize = 13.sp
        )
        Slider(
            value = state.zoomSpeed,
            onValueChange = { state.zoomSpeed = it },
            valueRange = 0.01f..0.12f,
            modifier = Modifier.width(180.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00D4FF),
                activeTrackColor = Color(0xFF00D4FF)
            )
        )
        Text(
            text = "%.1fx".format(state.zoomSpeed),
            color = Color(0xFF00D4FF),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "Center: (%.6f, %.6f)  Zoom: %.2e".format(
                state.centerX, state.centerY, state.zoom
            ),
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )

        Spacer(Modifier.width(16.dp))

        Button(
            onClick = { state.reset() },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF333355),
                contentColor = Color.White
            )
        ) {
            Text("Reset")
        }
    }
}

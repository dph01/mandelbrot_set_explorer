# Mandelbrot Set Explorer — Compose Desktop App

A Kotlin Compose Desktop (JVM) application that renders the Mandelbrot set, allows clicking to zoom into a point, and has a slider to control zoom speed.

## Project Structure

Single-module Compose Desktop project using Gradle with Kotlin DSL.

```
build.gradle.kts
settings.gradle.kts
gradle.properties
src/main/kotlin/mandelbrot/
  Main.kt
  MandelbrotState.kt
  MandelbrotRenderer.kt
  MandelbrotCanvas.kt
  ColorMapper.kt
  Controls.kt
```

## Build Configuration

- Kotlin JVM 1.9.21 with JetBrains Compose plugin 1.5.11
- Dependency: `kotlinx-coroutines-core:1.7.3`
- Main class: `mandelbrot.MainKt`
- JVM args: `-Xmx2048m`
- Native distribution formats: Dmg, Msi, Deb (package name `mandelbrot-explorer`, version `1.0.0`)
- Run with `./gradlew run`

## Application Window (`Main.kt`)

Opens a single window titled "Mandelbrot Set Explorer" at 1024×768 dp. The layout is a vertical `Column`:
1. `Controls` toolbar at the top
2. `MandelbrotCanvas` filling the remaining space (`Modifier.weight(1f)`)

A single `MandelbrotState` instance is created via `remember` and shared between both composables.

## State (`MandelbrotState`)

A class holding all Mandelbrot viewer state using Compose `mutableStateOf` properties:

| Property | Type | Default | Description |
|---|---|---|---|
| `centerX` | Double | -0.5 | Real component of the view center |
| `centerY` | Double | 0.0 | Imaginary component of the view center |
| `zoom` | Double | 1.0 | Zoom level (multiplicative) |
| `zoomSpeed` | Float | 0.037 | Zoom rate factor; range 0.01–0.12 |
| `isZooming` | Boolean | false | Whether zoom animation is active |
| `targetX` | Double | 0.0 | Real component of the zoom target |
| `targetY` | Double | 0.0 | Imaginary component of the zoom target |

Constants: `MAX_ITERATIONS = 256`.

### Methods

- **`reset()`** — Restores `centerX`, `centerY`, `zoom` to defaults and sets `isZooming` to false.
- **`setTarget(cx, cy)`** — Sets the target coordinates and enables zooming.
- **`screenToComplex(screenX, screenY, width, height)`** — Converts screen pixel coordinates to complex-plane coordinates. Uses `scale = 3.0 / (zoom * min(width, height))`, then offsets from center by the pixel distance from the canvas midpoint times this scale.

## Renderer (`MandelbrotRenderer`)

A singleton object with a single `render` method.

### `render(width, height, centerX, centerY, zoom, maxIterations = 256) → ImageBitmap`

1. Clamp width and height to at least 1.
2. Compute `scale = 3.0 / (zoom * min(width, height))`.
3. For each pixel `(px, py)`, compute the corresponding complex coordinate `c = (centerX + (px - w/2) * scale, centerY + (py - h/2) * scale)`.
4. Run the standard Mandelbrot iteration (`z = z² + c`) up to `maxIterations`, escaping when `|z|² > 4`.
5. **Parallelization**: Rows are computed in parallel using `IntStream.range(0, h).parallel()`. Each row is independent.
6. **Color mapping is inlined** per pixel to avoid object allocation:
   - Points that don't escape within `maxIterations`: black (`0xFF000000`).
   - Escaped points: compute smooth iteration count as `iteration + 1 - ln(ln(|z|²) / 2) / ln(2)`, then map to HSB with `hue = (smoothed * 7) % 360`, saturation 0.85, brightness 1.0. Convert HSB to ARGB integer via manual sector-based conversion.
7. Write ARGB pixel values into an `IntArray`, create a `BufferedImage(TYPE_INT_ARGB)`, call `setRGB`, and convert to `ImageBitmap` via `toComposeImageBitmap()`.

## Color Mapper (`ColorMapper`)

A singleton object providing `map(iterations, maxIterations, zReal, zImag) → Color`. Uses the same smooth coloring algorithm as the inlined renderer code (smooth iteration count, HSB with `hue = (smoothed * 7) % 360`, saturation 0.85, brightness 1.0). Returns `Color.Black` for points that didn't escape. Contains a private `hsbToColor` helper that performs sector-based HSB-to-RGB conversion.

> Note: This object exists as a standalone utility but is not called by the renderer at runtime — the renderer inlines equivalent logic for performance.

## Canvas (`MandelbrotCanvas`)

A composable that displays the rendered Mandelbrot image and handles user interaction.

### Rendering Pipeline

- Maintains local state: `canvasSize` (IntSize, default 800×600), `imageBitmap`, and a `renderJob` (coroutine Job).
- **`requestRender()`**: Cancels any in-flight render job, then launches a new coroutine that:
  1. Reads current `canvasSize`, `centerX`, `centerY`, `zoom`.
  2. If `isZooming`, divides canvas dimensions by 2 (half-resolution) for performance. Otherwise renders at full resolution.
  3. Calls `MandelbrotRenderer.render(...)` on `Dispatchers.Default`.
  4. Assigns the result to `imageBitmap`.

### Render Triggers

- **Initial render**: `LaunchedEffect(Unit)` calls `requestRender()` once.
- **Canvas resize**: `LaunchedEffect(canvasSize)` re-renders on size change.
- **Zoom stops**: `LaunchedEffect(state.isZooming)` triggers a full-resolution render when `isZooming` becomes false.

### Zoom Animation Loop

A `LaunchedEffect` keyed on `(isZooming, targetX, targetY, zoomSpeed)`:
- While `isZooming` is true, loops with `delay(16)` (~60 fps target).
- Each frame computes `dt` from `System.nanoTime()` delta in seconds.
- Zoom: `state.zoom *= (1.0 + state.zoomSpeed * dt)`.
- Pan: Center eases toward target with `easeRate = min(1.0, 2.0 * dt)`. Each axis: `center += (target - center) * easeRate`.
- Calls `requestRender()` each frame.

### Click Handling

Uses `detectTapGestures(onPress = ...)`. On press, converts the tap position to complex coordinates via `state.screenToComplex(...)` and calls `state.setTarget(re, im)`, which starts the zoom animation.

### Layout

A `Box` with black background, `fillMaxSize`, `onSizeChanged` listener, and pointer input. Inside, the current `imageBitmap` (if non-null) is displayed as an `Image` with `ContentScale.FillBounds` and `FilterQuality.Low`.

## Controls (`Controls`)

A horizontal toolbar row with dark background (`#1A1A2E`), 16dp horizontal / 8dp vertical padding.

Contents, left to right:
1. **"Zoom Speed:"** label — white, 13sp.
2. **Slider** — range 0.01..0.12, 180dp wide, cyan accent (`#00D4FF`). Bound to `state.zoomSpeed`.
3. **Speed value display** — formatted as `"%.1fx"`, cyan monospace 13sp.
4. **Spacer** (fills remaining space).
5. **Coordinates/zoom display** — formatted as `"Center: (%.6f, %.6f)  Zoom: %.2e"`, grey (`#AAAAAA`) monospace 12sp.
6. **16dp spacer**.
7. **"Reset" button** — dark purple background (`#333355`), white text. Calls `state.reset()`.

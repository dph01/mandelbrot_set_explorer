# Mandelbrot Set Explorer

A desktop Mandelbrot set viewer built with Kotlin and Jetbrains Compose. Click anywhere to zoom into that region with smooth, continuous animation.

## Features

- **Click-to-zoom** with smooth easing toward the target point
- **Parallel rendering** — row computation is distributed across all CPU cores via Java parallel streams
- **Adaptive resolution** — renders at half resolution during zoom animation for responsiveness, full resolution when idle
- **Smooth coloring** — fractional escape-time algorithm with HSB color mapping
- **Adjustable zoom speed** via slider control
- **Real-time coordinates** — displays current center position and zoom level

## Requirements

- JDK 17+
- macOS, Windows, or Linux

## Running

```bash
./gradlew run
```

## Controls

- **Click** on the canvas to start zooming toward that point
- **Zoom Speed slider** adjusts animation speed (0.01x–0.12x)
- **Reset** button returns to the default view

## Building a native package

```bash
./gradlew package
```

Produces platform-specific installers (`.dmg` on macOS, `.msi` on Windows, `.deb` on Linux).

## Tech stack

- Kotlin 1.9 / JVM
- Jetbrains Compose Desktop 1.5
- Kotlin Coroutines 1.7
- Java parallel streams for multi-core rendering

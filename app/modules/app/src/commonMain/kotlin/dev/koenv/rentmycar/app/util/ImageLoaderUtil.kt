package dev.koenv.rentmycar.app.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.util.DebugLogger
import dev.koenv.rentmycar.shared.SharedModule
import okio.FileSystem

/**
 * Creates and configures an ImageLoader for Coil3 with support for all common image formats:
 * - PNG, JPEG (built-in Android decoders)
 * - SVG (vector graphics)
 * - GIF (animated images)
 * - WebP (built-in Android decoder on API 14+)
 * - HEIF/HEIC (built-in Android decoder on API 28+)
 * 
 * Also includes:
 * - Ktor network fetching (reusing the app's HttpClient)
 * - Memory and disk caching
 * - Crossfade animations
 * - Debug logging for troubleshooting
 */
fun createImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context)
        .components {
            // Use Ktor for network fetching with the shared HttpClient
            add(KtorNetworkFetcherFactory(httpClient = SharedModule.provideHttpClient()))
            
            // Add decoders for common image formats
            add(SvgDecoder.Factory())  // SVG support
            
            // GIF support - use GifDecoder (compatible across all supported targets)
            add(GifDecoder.Factory())
            
            // Note: PNG, JPEG, WebP (static), BMP, HEIF/HEIC are handled by Android's built-in decoders
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                .maxSizeBytes(512L * 1024 * 1024) // 512MB
                .build()
        }
        // Enable crossfade animation by default
        .crossfade(true)
        // Enable debug logging to help troubleshoot image loading issues
        .logger(DebugLogger())
        .build()
}

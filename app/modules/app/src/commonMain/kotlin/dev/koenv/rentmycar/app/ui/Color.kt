package dev.koenv.rentmycar.app.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color palette for the application theme.
 * Defines semantic color tokens for light and dark modes.
 */

val Black: Color = Color(0xFF000000)
val Gray900: Color = Color(0xFF282828)
val Gray800: Color = Color(0xFF4b4b4b)
val Gray700: Color = Color(0xFF5e5e5e)
val Gray600: Color = Color(0xFF727272)
val Gray500: Color = Color(0xFF868686)
val Gray400: Color = Color(0xFFC7C7C7)
val Gray300: Color = Color(0xFFDFDFDF)
val Gray200: Color = Color(0xFFE2E2E2)
val Gray100: Color = Color(0xFFF7F7F7)
val Gray50: Color = Color(0xFFFFFFFF)
val White: Color = Color(0xFFFFFFFF)

val Red900: Color = Color(0xFF520810)
val Red800: Color = Color(0xFF950f22)
val Red700: Color = Color(0xFFbb032a)
val Red600: Color = Color(0xFFde1135)
val Red500: Color = Color(0xFFf83446)
val Red400: Color = Color(0xFFfc7f79)
val Red300: Color = Color(0xFFffb2ab)
val Red200: Color = Color(0xFFffd2cd)
val Red100: Color = Color(0xFFffe1de)
val Red50: Color = Color(0xFFfff0ee)

val Blue900: Color = Color(0xFF276EF1)
val Blue800: Color = Color(0xFF3F7EF2)
val Blue700: Color = Color(0xFF578EF4)
val Blue600: Color = Color(0xFF6F9EF5)
val Blue500: Color = Color(0xFF87AEF7)
val Blue400: Color = Color(0xFF9FBFF8)
val Blue300: Color = Color(0xFFB7CEFA)
val Blue200: Color = Color(0xFFCFDEFB)
val Blue100: Color = Color(0xFFE7EEFD)
val Blue50: Color = Color(0xFFFFFFFF)

val Green950: Color = Color(0xFF0B4627)
val Green900: Color = Color(0xFF16643B)
val Green800: Color = Color(0xFF1A7544)
val Green700: Color = Color(0xFF178C4E)
val Green600: Color = Color(0xFF1DAF61)
val Green500: Color = Color(0xFF1FC16B)
val Green400: Color = Color(0xFF3EE089)
val Green300: Color = Color(0xFF84EBB4)
val Green200: Color = Color(0xFFC2F5DA)
val Green100: Color = Color(0xFFD0FBE9)
val Green50: Color = Color(0xFFE0FAEC)

@Immutable
data class Colors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val success: Color,
    val onSuccess: Color,
    val disabled: Color,
    val onDisabled: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val background: Color,
    val onBackground: Color,
    val outline: Color,
    val transparent: Color = Color.Transparent,
    val white: Color = White,
    val black: Color = Black,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val scrim: Color,
    val elevation: Color,
)

internal val LightColors =
    Colors(
        primary = Black,
        onPrimary = White,
        primaryContainer = Blue100,
        onPrimaryContainer = Blue900,
        secondary = Gray400,
        onSecondary = Black,
        secondaryContainer = Gray100,
        onSecondaryContainer = Gray900,
        tertiary = Blue900,
        onTertiary = White,
        tertiaryContainer = Blue100,
        onTertiaryContainer = Blue900,
        surface = Gray200,
        onSurface = Black,
        surfaceVariant = Gray100,
        onSurfaceVariant = Gray900,
        error = Red600,
        onError = White,
        errorContainer = Red100,
        onErrorContainer = Red900,
        success = Green600,
        onSuccess = White,
        disabled = Gray100,
        onDisabled = Gray500,
        background = White,
        onBackground = Black,
        outline = Gray300,
        transparent = Color.Transparent,
        white = White,
        black = Black,
        text = Black,
        textSecondary = Gray700,
        textDisabled = Gray400,
        scrim = Color.Black.copy(alpha = 0.32f),
        elevation = Gray700,
    )

internal val DarkColors =
    Colors(
        primary = White,
        onPrimary = Black,
        primaryContainer = Blue900,
        onPrimaryContainer = Blue100,
        secondary = Gray400,
        onSecondary = White,
        secondaryContainer = Gray800,
        onSecondaryContainer = Gray100,
        tertiary = Blue300,
        onTertiary = Black,
        tertiaryContainer = Blue800,
        onTertiaryContainer = Blue100,
        surface = Gray900,
        onSurface = White,
        surfaceVariant = Gray800,
        onSurfaceVariant = Gray100,
        error = Red400,
        onError = Black,
        errorContainer = Red900,
        onErrorContainer = Red100,
        success = Green700,
        onSuccess = Black,
        disabled = Gray700,
        onDisabled = Gray500,
        background = Black,
        onBackground = White,
        outline = Gray800,
        transparent = Color.Transparent,
        white = White,
        black = Black,
        text = White,
        textSecondary = Gray300,
        textDisabled = Gray600,
        scrim = Color.Black.copy(alpha = 0.72f),
        elevation = Gray200,
    )

val LocalColors = staticCompositionLocalOf { LightColors }
val LocalContentColor = compositionLocalOf { Color.Black }
val LocalContentAlpha = compositionLocalOf { 1f }

/**
 * Returns the appropriate content color for a given background color.
 * Used to ensure proper text/icon contrast on different background colors.
 *
 * @param backgroundColor The background color to match
 * @return The appropriate foreground color for contrast
 */
fun Colors.contentColorFor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        primary -> onPrimary
        primaryContainer -> onPrimaryContainer
        secondary -> onSecondary
        secondaryContainer -> onSecondaryContainer
        tertiary -> onTertiary
        tertiaryContainer -> onTertiaryContainer
        surface -> onSurface
        surfaceVariant -> onSurfaceVariant
        error -> onError
        errorContainer -> onErrorContainer
        success -> onSuccess
        disabled -> onDisabled
        background -> onBackground
        else -> Color.Unspecified
    }
}

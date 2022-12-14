package com.example.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme as M3MaterialTheme

@Composable
fun Theme(
    themeColors: ThemeColors = ThemeColors.Default,
    isDarkMode: Boolean,
    appActions: AppActions,
    content: @Composable () -> Unit
) {
    M3MaterialThemeSetup(themeColors, isDarkMode) {
        MaterialTheme(colors = themeColors.getTheme(isDarkMode).animate()) {
            CompositionLocalProvider(
                LocalAppActions provides appActions,
                LocalThemeSpacing provides remember { Spacing() },
                LocalMainScrollState provides rememberLazyListState()
            ) { content() }
        }
    }
}

@Suppress("unused")
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeSpacing.current

@Suppress("unused")
val M3MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalThemeSpacing.current

val LocalAppActions = staticCompositionLocalOf<AppActions> { error("No Actions") }

val LocalMainScrollState = staticCompositionLocalOf<LazyListState> { error("No Actions") }

@Composable
private fun Colors.animate() = copy(
    primary = primary.animate().value,
    primaryVariant = primaryVariant.animate().value,
    onPrimary = onPrimary.animate().value,
    secondary = secondary.animate().value,
    secondaryVariant = secondaryVariant.animate().value,
    onSecondary = onSecondary.animate().value,
    background = background.animate().value,
    onBackground = onBackground.animate().value,
    surface = surface.animate().value,
    onSurface = onSurface.animate().value,
    onError = onError.animate().value,
    error = error.animate().value,
    isLight = isLight
)

@Composable
fun ColorScheme.animate() = copy(
    primary.animate().value,
    onPrimary.animate().value,
    primaryContainer.animate().value,
    onPrimaryContainer.animate().value,
    inversePrimary.animate().value,
    secondary.animate().value,
    onSecondary.animate().value,
    secondaryContainer.animate().value,
    onSecondaryContainer.animate().value,
    tertiary.animate().value,
    onTertiary.animate().value,
    tertiaryContainer.animate().value,
    onTertiaryContainer.animate().value,
    background.animate().value,
    onBackground.animate().value,
    surface.animate().value,
    onSurface.animate().value,
    surfaceVariant.animate().value,
    onSurfaceVariant.animate().value,
    surfaceTint.animate().value,
    inverseSurface.animate().value,
    inverseOnSurface.animate().value,
    error.animate().value,
    onError.animate().value,
    errorContainer.animate().value,
    onErrorContainer.animate().value,
    outline.animate().value,
)

@Composable
fun Color.animate() = animateColorAsState(this)

enum class ThemeColors(
    private val light: Colors = lightColors(),
    private val dark: Colors = darkColors(),
    private val lightScheme: ColorScheme = lightColorScheme(),
    private val darkScheme: ColorScheme = darkColorScheme()
) {
    Default(
        darkScheme = darkColorScheme(
            primary = Color(0xff8bd0f0),
            onPrimary = Color(0xff003546),
            primaryContainer = Color(0xff004d64),
            onPrimaryContainer = Color(0xffbee9ff),
            inversePrimary = Color(0xff126682),
            secondary = Color(0xffb4cad6),
            onSecondary = Color(0xff1f333c),
            secondaryContainer = Color(0xff354a54),
            onSecondaryContainer = Color(0xffd0e6f2),
            tertiary = Color(0xffc6c2ea),
            onTertiary = Color(0xff2f2d4d),
            tertiaryContainer = Color(0xff454364),
            onTertiaryContainer = Color(0xffe3dfff),
            background = Color(0xff191c1e),
            onBackground = Color(0xffe1e2e4),
            surface = Color(0xff191c1e),
            onSurface = Color(0xffe1e2e4),
            surfaceVariant = Color(0xff40484c),
            onSurfaceVariant = Color(0xffc5c7c9),
            inverseSurface = Color(0xffe1e2e4),
            inverseOnSurface = Color(0xff2e3133),
            outline = Color(0xff8a9297),
        ),
        lightScheme = lightColorScheme(
            primary = Color(0xff126682),
            onPrimary = Color(0xffffffff),
            primaryContainer = Color(0xffbee9ff),
            onPrimaryContainer = Color(0xff001f2a),
            inversePrimary = Color(0xff8bd0f0),
            secondary = Color(0xff4d616c),
            onSecondary = Color(0xffffffff),
            secondaryContainer = Color(0xffd0e6f2),
            onSecondaryContainer = Color(0xff081e27),
            tertiary = Color(0xff5d5b7d),
            onTertiary = Color(0xffffffff),
            tertiaryContainer = Color(0xffe3dfff),
            onTertiaryContainer = Color(0xff1a1836),
            background = Color(0xfffbfcfe),
            onBackground = Color(0xff191c1e),
            surface = Color(0xfffbfcfe),
            onSurface = Color(0xff191c1e),
            surfaceVariant = Color(0xffdce4e9),
            onSurfaceVariant = Color(0xff40484c),
            inverseSurface = Color(0xff2e3133),
            inverseOnSurface = Color(0xffeff1f3),
            outline = Color(0xff6f777c),
        )
    ),
    NoColors(
        lightColors(
            primary = Color(0xff2196F3),
            secondary = Color(0xff90CAF9)
        ),
        darkColors(
            primary = Color(0xff90CAF9),
            secondary = Color(0xff90CAF9)
        ),
        lightColorScheme(
            primary = Color(0xff2196F3),
            secondary = Color(0xff90CAF9)
        ),
        darkColorScheme(
            primary = Color(0xff90CAF9),
            secondary = Color(0xff90CAF9)
        )
    ),
    Red(
        createLightColors(
            first = Color(0xffbe0b07),
            second = Color(0xff705c2e),
            background = Color(0xffffdad5),
            surface = Color(0xfffbdfa6)
        ),
        createDarkColors(
            first = Color(0xffffb4a8),
            second = Color(0xffdec38c),
            background = Color(0xff930001),
            surface = Color(0xff564419)
        ),
        createLightColorScheme(
            first = Color(0xffbe0b07),
            second = Color(0xff705c2e),
            background = Color(0xffffdad5),
            surface = Color(0xfffbdfa6)
        ),
        createDarkColorScheme(
            first = Color(0xffffb4a8),
            second = Color(0xffdec38c),
            background = Color(0xff930001),
            surface = Color(0xff564419)
        )
    ),
    DarkBlue(
        createLightColors(
            first = Color(0xff005db6),
            second = Color(0xff555f71),
            background = Color(0xffd6e3ff),
            surface = Color(0xffd9e3f9)
        ),
        createDarkColors(
            first = Color(0xffa9c7ff),
            second = Color(0xffbdc7dc),
            background = Color(0xff00468b),
            surface = Color(0xff3e4758)
        ),
        createLightColorScheme(
            first = Color(0xff005db6),
            second = Color(0xff555f71),
            background = Color(0xffd6e3ff),
            surface = Color(0xffd9e3f9)
        ),
        createDarkColorScheme(
            first = Color(0xffa9c7ff),
            second = Color(0xffbdc7dc),
            background = Color(0xff00468b),
            surface = Color(0xff3e4758)
        )
    ),
    Green(
        createLightColors(
            first = Color(0xff006c48),
            second = Color(0xff4d6356),
            background = Color(0xff8df7c2),
            surface = Color(0xffd0e8d8)
        ),
        createDarkColors(
            first = Color(0xff71dba7),
            second = Color(0xffb4ccbc),
            background = Color(0xff005235),
            surface = Color(0xff364b3f)
        ),
        createLightColorScheme(
            first = Color(0xff006c48),
            second = Color(0xff4d6356),
            background = Color(0xff8df7c2),
            surface = Color(0xffd0e8d8)
        ),
        createDarkColorScheme(
            first = Color(0xff71dba7),
            second = Color(0xffb4ccbc),
            background = Color(0xff005235),
            surface = Color(0xff364b3f)
        )
    );

    fun getTheme(darkTheme: Boolean) = if (darkTheme) dark else light
    fun getThemeScheme(darkTheme: Boolean) = if (darkTheme) darkScheme else lightScheme
}

private fun createLightColors(first: Color, second: Color, background: Color, surface: Color) = lightColors(
    primary = first,
    primaryVariant = second,
    onPrimary = Color.White,
    secondary = first,
    secondaryVariant = second,
    onSecondary = Color.White,
    background = background,
    onBackground = Color.Black,
    surface = surface,
    onSurface = Color.Black
)

private fun createDarkColors(first: Color, second: Color, background: Color, surface: Color) = darkColors(
    primary = first,
    primaryVariant = second,
    onPrimary = Color.Black,
    secondary = first,
    secondaryVariant = second,
    onSecondary = Color.Black,
    background = background,
    onBackground = Color.White,
    surface = surface,
    onSurface = Color.White
)

private fun createLightColorScheme(first: Color, second: Color, background: Color, surface: Color) = lightColorScheme(
    primary = first,
    onPrimaryContainer = second,
    onPrimary = Color.White,
    secondary = first,
    secondaryContainer = surface,
    onSecondaryContainer = second,
    onSecondary = Color.White,
    background = background,
    onBackground = Color.Black,
    surface = surface,
    onSurface = Color.Black
)

private fun createDarkColorScheme(first: Color, second: Color, background: Color, surface: Color) = darkColorScheme(
    primary = first,
    onPrimaryContainer = second,
    onPrimary = Color.Black,
    secondary = first,
    secondaryContainer = surface,
    onSecondaryContainer = second,
    onSecondary = Color.Black,
    background = background,
    onBackground = Color.White,
    surface = surface,
    onSurface = Color.White
)

data class Spacing(
    val xs: Dp = 2.dp,
    val s: Dp = 4.dp,
    val m: Dp = 8.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp
)

internal val LocalThemeSpacing: ProvidableCompositionLocal<Spacing> = staticCompositionLocalOf { Spacing() }
package com.mathematics.squares.presentation.view.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.mathematics.squares.presentation.model.Settings

private val DarkColorScheme = darkColorScheme(
    primary = OrangeDark,
    primaryContainer = Color(0xFF39393C),
    onPrimary = Color.White,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = BackgroundDark,
    onBackground = Color.White,
    onPrimaryContainer = DarkerGray
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeLight,
    primaryContainer = Color(0xFFEDEEF3),
    onPrimary = Color.Black,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White,
    onBackground = Color.Black,
    onPrimaryContainer = DarkerGray

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


@Composable
fun Color.animate(stiffness: Float = Spring.StiffnessMediumLow): Color =
    animateColorAsState(
        targetValue = this,
        animationSpec = spring(stiffness = stiffness)
    ).value

@Composable
fun Float.animate(stiffness: Float = Spring.StiffnessMediumLow): Float =
    animateFloatAsState(
        targetValue = this,
        animationSpec = spring(stiffness = stiffness)
    ).value

@Composable
fun Dp.animate(stiffness: Float = Spring.StiffnessMediumLow): Dp =
    animateDpAsState(
        targetValue = this,
        animationSpec = spring(stiffness = stiffness)
    ).value

@Composable
fun PaddingValues.animate(stiffness: Float = Spring.StiffnessMediumLow): PaddingValues {
    return PaddingValues(
        top = this.calculateTopPadding().animate(stiffness),
        bottom = this.calculateBottomPadding().animate(stiffness),
        start = this.calculateStartPadding(LayoutDirection.Ltr).animate(stiffness),
        end = this.calculateEndPadding(LayoutDirection.Ltr).animate(stiffness)
    )
}



@Composable
fun SquaresTheme(
    settingsLiveData: LiveData<Settings> = liveData { emit(Settings()) },
    statusBarColor: Color? = null,
    dynamicColor: Boolean = true,
    content: @Composable (Themes) -> Unit
) {
    val settings by settingsLiveData.observeAsState(Settings())

    val isDarkTheme = when (settings.theme) {
        Themes.Light -> false
        Themes.Dark -> true
        null -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val statusBarColorArgb = (statusBarColor ?: colorScheme.primary)
        .animate(stiffness = Spring.StiffnessHigh)
        .toArgb()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBarColorArgb
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = dynamicColor == isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = { content(settings.theme.castToNotNull()) }
    )
}
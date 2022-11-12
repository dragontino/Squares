package com.mathematics.squares.presentation.view.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.ModeNight
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class Themes(val icon: ImageVector) {
    Light(Icons.Rounded.ModeNight),
    Dark(Icons.Rounded.LightMode);

    fun switch(): Themes = when (this) {
        Light -> Dark
        Dark -> Light
    }
}

@Composable
fun Themes?.castToNotNull() = when {
    this != null -> this
    isSystemInDarkTheme() -> Themes.Dark
    else -> Themes.Light
}
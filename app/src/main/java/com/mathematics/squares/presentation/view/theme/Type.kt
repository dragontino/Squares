package com.mathematics.squares.presentation.view.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(RobotoFont),
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(RobotoFont),
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(RobotoFont),
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(RobotoFont),
        fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily(BeautifulFont),
        fontSize = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily(BeautifulFont),
        fontSize = 16.sp,
        letterSpacing = 3.sp,
        shadow = Shadow(Color.DarkGray),
        background = Color.Transparent,
        baselineShift = BaselineShift.Superscript
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily(BeautifulFont),
        fontSize = 13.sp,
        letterSpacing = 3.sp,
        shadow = Shadow(Color.DarkGray),
        textIndent = TextIndent(3.sp, 3.sp)
    )
    /* Other default text styles to override
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
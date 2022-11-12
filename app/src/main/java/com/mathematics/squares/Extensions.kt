package com.mathematics.squares

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

operator fun Color.plus(other: Color): Color {
    return layer(other)
}

fun Color.layer(other: Color): Color {
    val a2 = other.alpha
    if (a2 == 0f) return this

    val a1 = this.alpha

    // Результирующая прозрачность вычисляется по формуле:
    // na = na1 * na2, - где na - величина, обратная прозрачности (na = 1-a).
    // Следовательно:
    // a = 1 - (1 - a1) * (1 - a2) = a1 + a2 - a1 * a2.
    val a = a1 + a2 - (a1 * a2) / 255f

    val alpha = a2 / a
    val rAlpha = 1f - alpha
    val r = this.red * rAlpha + other.red * alpha
    val g = (this.green * rAlpha + other.green * alpha)
    val b = (this.blue * rAlpha + other.blue * alpha)

    return Color(alpha = a, red = r, green = g, blue = b)
}


fun Double.posRoundToInt() = (this + 0.5).roundToInt()
fun Float.posRoundToInt() = (this + 0.5).roundToInt()

fun Color.mix(weight1: Float, color2: Color, weight2: Float): Color {
    val aw1 = this.alpha * weight1
    val aw2 = color2.alpha * weight2
    val a = aw1 + aw2

    if (a == 0f) return Color.Transparent

    // Если при расчёте каналов r, g, b мы будем использовать указанные веса, когда они
    // в сумме не составляют 1, то на самом деле к цвету мы незаметно будем примешивать
    // чёрный цвет (r=0, g=0, b=0). Белый цвет совершенно безосновательно начнёт сереть.
    // (В Gimp'е почему-то именно так и происходит! Сделайте изображение из двух половин -
    // белой и прозрачной, - уменьшите его до 1 пикселя и посмотрите на результат.
    // Неожиданно!) Именно поэтому пропорционально увеличиваем веса (с учётом канала a!),
    // чтобы в сумме они составили 1.

    val cWeight1 = aw1 / a
    val cWeight2 = 1.0 - cWeight1 // тоже самое, что и aw2 / a
    val r = (this.red * cWeight1 + color2.red * cWeight2).posRoundToInt()
    val g = (this.green * cWeight1 + color2.green * cWeight2).posRoundToInt()
    val b = (this.blue * cWeight1 + color2.blue * cWeight2).posRoundToInt()

    return Color(alpha = a.posRoundToInt(), red = r, green = g, blue = b)
}
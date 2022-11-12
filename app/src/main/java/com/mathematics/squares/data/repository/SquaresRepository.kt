package com.mathematics.squares.data.repository

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.mathematics.squares.presentation.model.Cords
import com.mathematics.squares.presentation.model.Square
import com.mathematics.squares.presentation.model.Matrix
import com.mathematics.squares.presentation.model.set
import com.mathematics.squares.presentation.view.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SquaresRepository {
    private val colors = arrayOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Magenta, Color.Cyan, Purple40, Purple80,
        OrangeDark, OrangeLight, Pink80, Pink40
    )

    fun getRandomColor() = colors.apply { shuffle() }[0]

    suspend fun calculateSquaresPositions(
        squareLength: Int,
        x5: Int,
        x4: Int,
        x3: Int,
        x2: Int
    ): Map<Cords, Square>? /*Square?*/ {

        val matrix = Matrix(squareLength)
        val squaresMap = HashMap<Cords, Square>()

        if (25 * x5 + 16 * x4 + 9 * x3 + 4 * x2 > matrix.area)
            return null

        colors.shuffle()


        val result = findSquarePlace(
            fieldSize = matrix.length,
            countMap = mapOf(5 to x5, 4 to x4, 3 to x3, 2 to x2)
        ) { x, y, length, color ->
            matrix.set(x, y, length, color).also {
                if (it) squaresMap[x, y] = Square(length, color)
            }
        }

        matrix.filter { !it.isOccupied }.forEach {
            val color = getRandomColor()
            matrix[it.x, it.y, 1] = color
            squaresMap[it] = Square(1, color)
        }

        Log.d("SquaresRepo", "Свободное место = ${matrix.freeSpace}")

        return squaresMap //square //if (result) square else null
    }


    private suspend fun findSquarePlace(
        countMap: Map<Int, Int>,
        fieldSize: Int,
        startLength: Int = 5,
        startIndex: Int = 0,
        fillArea: (x: Int, y: Int, length: Int, color: Color) -> Boolean
    ): Boolean {
        if (startLength < 1)
            return true

        val maxRepeat = 1000
        val cordsList = ArrayList<Cords>()
        var repeatersCount = 0
        val steps = countMap[startLength] ?: 0

        suspend fun generateCords() = withContext(Dispatchers.Main) {
            var x: Int
            var y: Int

            for (i in startIndex until steps + startIndex) {
                do {
                    x = Random.nextInt(0, fieldSize - startLength + 1)
                    y = Random.nextInt(0, fieldSize - startLength + 1)
                    repeatersCount++
                } while (
                    !fillArea(x, y, startLength, colors[i % colors.size]) &&
                    repeatersCount < maxRepeat ||
                    Cords(x, y) in cordsList
                )
                if (repeatersCount > maxRepeat) {
                    cordsList.clear()
                    return@withContext null
                }
                cordsList += Cords(x, y)
            }
        }

        return if (generateCords() == null) {
            Log.d("SquaresRepo", "repeatersCount = $repeatersCount")
            false
        }
        else {
            val result = findSquarePlace(
                startLength = startLength - 1,
                startIndex = startIndex + steps,
                countMap = countMap,
                fieldSize = fieldSize,
                fillArea = fillArea
            )

            return if (!result) generateCords() != null else true
        }
    }
}
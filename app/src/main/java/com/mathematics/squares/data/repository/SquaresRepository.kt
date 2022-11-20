package com.mathematics.squares.data.repository

import androidx.compose.ui.graphics.Color
import com.mathematics.squares.presentation.model.Cords
import com.mathematics.squares.presentation.model.Matrix
import com.mathematics.squares.presentation.model.Square
import com.mathematics.squares.presentation.view.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.shuffle
import kotlin.collections.shuffled
import kotlin.random.Random

class SquaresRepository {
    private val colors = arrayOf(
        Color.Red, Color.Blue, Color.Green, Color.Yellow,
        Color.Magenta, Color.Cyan, Purple40, Purple80,
        OrangeDark, OrangeLight, Pink80, Pink40
    )

    private companion object {
        const val secondsToWait = 8
    }

    fun getRandomColor() = colors.apply { shuffle() }[0]


    suspend fun updateSquaresPositions(
        squareSideLength: Int,
        squaresList: List<Square>,
    ): Map<Cords, Square> = withContext(Dispatchers.Default) {
        var result: Map<Cords, Square>?
        val startTime = System.currentTimeMillis()
        do {
            val shuffledList = squaresList.shuffled()
            result = placeSquares(squareSideLength, shuffledList)
            val currentTime = System.currentTimeMillis()
        } while (result == null && currentTime - startTime < secondsToWait * 1000)

        return@withContext result ?: placeSquares(squareSideLength, squaresList) ?: emptyMap()
    }



    suspend fun placeSquares(
        squareSideLength: Int,
        squaresList: List<Square>,
    ): Map<Cords, Square>? {
        val matrix = Matrix(squareSideLength)
        val result = HashMap<Cords, Square>()

        var startPosition = 0
        val loopDirection = getRandomDirection()
        val startVertex = getRandomStartVertex()


        suspend fun placeSquare(square: Square): Cords? {
            for (position in startPosition until squareSideLength * squareSideLength) {
                val cords = getNextCords(position, squareSideLength, loopDirection, startVertex)

                val topLeftCords = withContext(Dispatchers.Main) {
                    when (startVertex) {
                        StartVertex.TopLeft -> matrix.fillAreaByTopLeft(cords, square)
                        StartVertex.TopRight -> matrix.fillAreaByTopRight(cords, square)
                        StartVertex.BottomLeft -> matrix.fillAreaByBottomLeft(cords, square)
                        StartVertex.BottomRight -> matrix.fillAreaByBottomRight(cords, square)
                    }
                }

                if (topLeftCords != null) {
                    startPosition = position + square.sideLength
                    return topLeftCords
                }
            }
            return null
        }


        squaresList.forEach { square ->
            val cords = placeSquare(square) ?: return null
            result[cords] = square
        }

        matrix.filter { !it.isOccupied }.forEach {
            withContext(Dispatchers.Default) {
                val color = getRandomColor()
                matrix[it.x, it.y, 1] = color
                result[it] = Square(1, color)
            }
        }

        return result
    }

    private fun getNextCords(
        position: Int,
        sideLength: Int,
        loopDirection: LoopDirection,
        startVertex: StartVertex,
    ): Cords {
        val leftToRightY = position % sideLength
        val rightToLeftY = sideLength - position % sideLength - 1
        val topToBottomX = position / sideLength
        val bottomToTopX = sideLength - position / sideLength - 1

        val cords = when (startVertex) {
            StartVertex.TopLeft -> {
                when (loopDirection) {
                    LoopDirection.Parallel -> Cords(
                        x = topToBottomX,
                        y = leftToRightY
                    )
                    LoopDirection.Consistently -> Cords(
                        x = topToBottomX,
                        y = if (topToBottomX % 2 == 0) leftToRightY else rightToLeftY
                    )
                }
            }
            StartVertex.TopRight -> {
                when (loopDirection) {
                    LoopDirection.Consistently -> Cords(
                        x = topToBottomX,
                        y = if (topToBottomX % 2 != 0) leftToRightY else rightToLeftY
                    )
                    LoopDirection.Parallel -> Cords(
                        x = topToBottomX,
                        y = rightToLeftY
                    )
                }
            }
            StartVertex.BottomLeft -> {
                when (loopDirection) {
                    LoopDirection.Consistently -> Cords(
                        x = bottomToTopX,
                        y = if (topToBottomX % 2 == 0) leftToRightY else rightToLeftY
                    )
                    LoopDirection.Parallel -> Cords(
                        x = bottomToTopX,
                        y = leftToRightY,
                    )
                }
            }
            StartVertex.BottomRight -> {
                when (loopDirection) {
                    LoopDirection.Consistently -> Cords(
                        x = bottomToTopX,
                        y = if (topToBottomX % 2 != 0) leftToRightY else rightToLeftY,
                    )
                    LoopDirection.Parallel -> Cords(
                        x = bottomToTopX,
                        y = rightToLeftY,
                    )
                }
            }
        }

        return if (loopDirection.horizontally) {
            cords
        } else when (startVertex) {
            StartVertex.TopLeft -> cords.transposeRelativeMainDiagonal()
            StartVertex.TopRight -> cords.transposeRelativeSideDiagonal(sideLength)
            StartVertex.BottomLeft -> cords.transposeRelativeSideDiagonal(sideLength)
            StartVertex.BottomRight -> cords.transposeRelativeMainDiagonal()
        }
    }

    private fun getRandomDirection() = with(LoopDirection.objects()) {
        this[Random.nextInt(until = size)].apply {
            if (Random.nextInt(2) != 0) transpose()
        }
    }

    private fun getRandomStartVertex() = with(StartVertex.values()) {
        this[Random.nextInt(until = size)]
    }


    private sealed class LoopDirection {
        var horizontally: Boolean = true
        private set

        object Parallel : LoopDirection()
        object Consistently : LoopDirection()

        companion object {
            fun objects() = arrayOf(Parallel, Consistently)
        }

        fun transpose() {
            horizontally = !horizontally
        }
    }

    private enum class StartVertex {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight;
    }

    private fun Cords.transposeRelativeMainDiagonal() =
        Cords(x = this.y, y = this.x)

    private fun Cords.transposeRelativeSideDiagonal(size: Int) =
        Cords(x = size - this.y - 1, y = size - this.x - 1)
}
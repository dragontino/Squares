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
        OrangeDark, OrangeLight, Pink80, Pink40,
        Color.Gray, Color.DarkGray
    )

    private companion object {
        const val secondsToWait = 12
    }

    fun getRandomColor() = shuffleColors()[0]
    fun shuffleColors() = colors.apply { shuffle() }


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
        fieldSideLength: Int,
        squaresList: List<Square>,
    ): Map<Cords, Square>? {
        val matrix = Matrix(fieldSideLength)
        val result = HashMap<Cords, Square>()

        val tempList = squaresList.toMutableList()
        val loopDirection = getRandomDirection()
        val startVertex = getRandomStartVertex()

        val newColors = shuffleColors()

        if (squaresList.isNotEmpty()) {
            for (position in 0 until fieldSideLength * fieldSideLength) {
                var index = 0
                label@
                do {
                    val square = tempList[index]
                    val cords = getNextCords(position, fieldSideLength, loopDirection, startVertex)
                    val topLeftCords = withContext(Dispatchers.Main) {
                        when (startVertex) {
                            StartVertex.TopLeft -> matrix.fillAreaByTopLeft(cords, square)
                            StartVertex.TopRight -> matrix.fillAreaByTopRight(cords, square)
                            StartVertex.BottomLeft -> matrix.fillAreaByBottomLeft(cords, square)
                            StartVertex.BottomRight -> matrix.fillAreaByBottomRight(cords, square)
                        }
                    }

                    if (topLeftCords != null) {
                        tempList.remove(square)
                        result[topLeftCords] = square.copy(
                            color = if (square.sideLength == 1) {
                                Color.Transparent
                            } else {
                                newColors[square.sideLength - 1]
                            }
                        )
                        break@label
                    }
                    index++

                } while (index < tempList.size)

                if (tempList.isEmpty()) break
            }
        }

        if (tempList.isNotEmpty()) return null

        val colorForOne = Color.Transparent //getRandomColor()
        matrix.filter { !it.isOccupied }.forEach {
            withContext(Dispatchers.Default) {
                matrix[it.x, it.y, 1] = colorForOne
                result[it] = Square(1, colorForOne)
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
            fun objects() = arrayOf(Parallel) //, Consistently)
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
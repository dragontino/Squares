package com.mathematics.squares.presentation.model

import androidx.compose.ui.graphics.Color

class Matrix(private val length: Int) : java.io.Serializable {
    private val data = Array(length) {
        Array(length) { Cell() }
    }

    private fun <T> Array<Array<T>>.forEachIndexed(action: (i: Int, j: Int, T) -> Unit) {
        forEachIndexed { i, line ->
            line.forEachIndexed { j, item -> action(i, j, item) }
        }
    }

    private fun forEachIndexed(action: (i: Int, j: Int, Cell) -> Unit) {
        data.forEachIndexed(action)
    }

    private fun getOrNull(i: Int, j: Int) = data.getOrNull(i)?.getOrNull(j)

    operator fun get(i: Int, j: Int) = data[i][j]
    operator fun set(i: Int, j: Int, value: Cell) {
        data[i % length][j % length] = value
    }
    operator fun get(position: Int) = get(position / length, position % length)
    operator fun set(position: Int, value: Cell) =
        set(position / length, position % length, value)

    operator fun set(i: Int, j: Int, length: Int, color: Color) =
        fillArea(Cords(i, j), length, color) != null

    operator fun set(i: Int, j: Int, square: Square) =
        fillArea(Cords(i, j), square.sideLength, square.color)

    private fun <R> updateElementAt(i: Int, j: Int, action: Cell.() -> R) =
        getOrNull(i, j)?.action()

    private fun fillArea(topLeftCords: Cords, length: Int, color: Color): Cords? {
        synchronized(this) {
            if (!canFill(topLeftCords = topLeftCords, sideLength = length))
                return null

            for (i in topLeftCords.x until topLeftCords.x + length) {
                for (j in topLeftCords.y until topLeftCords.y + length) {
                    updateElementAt(i, j) {
                        isOccupied = true
                        backgroundColor = color
//                        if (
//                            i != topLeftCords.x && i != topLeftCords.x + length - 1
//                            && j != topLeftCords.y && j != topLeftCords.y + length - 1
//                        )
                        if (length > 1)
                            borderColor = color
                    }
                }
            }
            return topLeftCords
        }
    }

    fun fillAreaByTopLeft(topLeftCords: Cords, square: Square): Cords? {
        return fillArea(topLeftCords, square.sideLength, square.color)
    }

    fun fillAreaByBottomLeft(bottomLeftCords: Cords, square: Square): Cords? =
        fillArea(
            topLeftCords = bottomLeftCords - Cords(square.sideLength - 1, 0),
            color = square.color,
            length = square.sideLength,
        )

    fun fillAreaByBottomRight(bottomRightCords: Cords, square: Square): Cords? {
        return fillArea(
            topLeftCords = bottomRightCords - square.sideLength + 1,
            color = square.color,
            length = square.sideLength
        )
    }

    fun fillAreaByTopRight(topRightCords: Cords, square: Square): Cords? =
        fillArea(
            topLeftCords = topRightCords.copy(y = topRightCords.y - square.sideLength + 1),
            length = square.sideLength,
            color = square.color
        )


    fun filter(filterAction: (Cell) -> Boolean): List<Cords> {
        return buildList {
            forEachIndexed { i, j, squareItem ->
                if (filterAction(squareItem)) {
                    add(Cords(i, j))
                }
            }
        }
    }


    private fun canFill(topLeftCords: Cords, sideLength: Int): Boolean {
        for (i in topLeftCords.x until topLeftCords.x + sideLength) {
            for (j in topLeftCords.y until topLeftCords.y + sideLength) {
                if (getOrNull(i, j) == null || this[i, j].isOccupied) {
                    return false
                }
            }
        }
        return true
    }
}


data class Cell(
    var isOccupied: Boolean = false,
    var backgroundColor: Color? = null,
    var borderColor: Color? = null
)


data class Cords(
    val x: Int,
    val y: Int
) : Comparable<Cords> {
    operator fun plus(count: Int) = this + Cords(count, count)
    operator fun minus(count: Int) = this - Cords(count, count)
    operator fun plus(cords: Cords) = Cords(x + cords.x, y + cords.y)
    operator fun minus(cords: Cords) = Cords(x - cords.x, y - cords.y)
    override fun compareTo(other: Cords): Int {
        return when {
            this.x.compareTo(other.x) != 0 -> this.x.compareTo(other.x)
            else -> this.y.compareTo(other.y)
        }
    }
}

operator fun <V> HashMap<Cords, V>.set(x: Int, y: Int, value: V) {
    this[Cords(x, y)] = value
}

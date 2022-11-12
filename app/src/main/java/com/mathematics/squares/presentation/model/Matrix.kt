package com.mathematics.squares.presentation.model

import androidx.compose.ui.graphics.Color

class Matrix(val length: Int) : java.io.Serializable {
    constructor(length: Int, setter: Matrix.() -> Unit) : this(length) {
        setter()
    }

    private val data = Array(length) {
        Array(length) { SquareItem() }
    }

    private fun <T> Array<Array<T>>.forEachIndexed(action: (i: Int, j: Int, T) -> Unit) {
        forEachIndexed { i, line ->
            line.forEachIndexed { j, item -> action(i, j, item) }
        }
    }

    val area get() = length * length

    val freeSpace get() = data.flatten().filter { !it.isOccupied }.size

    fun forEachIndexed(action: (i: Int, j: Int, SquareItem) -> Unit) {
        data.forEachIndexed(action)
    }

    operator fun get(i: Int, j: Int) = data[i % length][j % length]
    operator fun set(i: Int, j: Int, value: SquareItem) {
        data[i % length][j % length] = value
    }
    operator fun get(position: Int) = get(position / length, position % length)
    operator fun set(position: Int, value: SquareItem) =
        set(position / length, position % length, value)

    operator fun set(i: Int, j: Int, length: Int, color: Color) =
        fillArea(Cords(i, j), length, color)

    fun <R> updateElementAt(position: Int, action: SquareItem.() -> R) =
        get(position).action()

    private fun <R> updateElementAt(i: Int, j: Int, action: SquareItem.() -> R) =
        get(i, j).action()

    private fun fillArea(topLeftCords: Cords, length: Int, color: Color): Boolean {
        synchronized(this) {
            if (!checkArea(topLeftCords, topLeftCords + length))
                return false

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
            return true
        }
    }

    fun clear() {
        data.flatten().forEach { it.clear() }
    }


    fun filter(filterAction: (SquareItem) -> Boolean): List<Cords> {
        return buildList {
            forEachIndexed { i, j, squareItem ->
                if (filterAction(squareItem)) {
                    add(Cords(i, j))
                }
            }
        }
    }


    private fun checkArea(topLeftCords: Cords, bottomRightCords: Cords): Boolean {
        for (i in topLeftCords.x until bottomRightCords.x)
            for (j in topLeftCords.y until bottomRightCords.y)
                if (this[i, j].isOccupied)
                    return false
        return true
    }
}


data class SquareItem(
    var isOccupied: Boolean = false,
    var backgroundColor: Color? = null,
    var borderColor: Color? = null
) {
    fun clear() {
        isOccupied = false
        backgroundColor = null
        borderColor = null
    }
}





data class Cords(
    val x: Int,
    val y: Int
) {
    operator fun plus(count: Int) = Cords(x + count, y + count)
}

operator fun <V> HashMap<Cords, V>.set(x: Int, y: Int, value: V) {
    this[Cords(x, y)] = value
}

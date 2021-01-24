package org.fuho.sheefra

import kotlin.math.abs

data class Position(val x: Int, val y: Int) {
    operator fun plus(v: Position) = Position(x + v.x, y + v.y)
}

data class Boundary(val a: Position, val b: Position) {
    val width = abs(b.x - a.x) + 1
    val height = abs(b.y - a.y) + 1
    operator fun contains(p: Position) = p.x in a.x..b.x && p.y in a.y..b.y
}

enum class CardinalDirection(val position: Position, val representation: Char) {
    NORTH(Position(0, -1), '↑'),
    EAST(Position(1, 0), '→'),
    SOUTH(Position(0, 1), '↓'),
    WEST(Position(-1, 0), '←');

    val x get() = position.x
    val y get() = position.y

    override fun toString() = representation.toString()
}

data class BoardField(var x: Int, var y: Int, val char: Char)

data class Board(val positions: Iterable<BoardField>) {

    private val minX get(): Int = positions.minOf { it.x }
    private val minY get(): Int = positions.minOf { it.y }
    private val maxX get(): Int = positions.maxOf { it.x }
    private val maxY get(): Int = positions.maxOf { it.y }

    private fun getPosition(x: Int, y: Int): BoardField? = positions.firstOrNull() {
        it.y == y && it.x == x
    }

    override fun toString(): String {
        val rows = mutableListOf<String>()
        for (row in minY..maxY) {
            val rowValues = mutableListOf<BoardField?>()
            for (col in minX..maxX) {
                rowValues.add(getPosition(col, row))
            }
            rows.add(rowValues
                .joinToString(
                    prefix = "\n│ ",
                    separator = " │ ",
                    postfix = " │",
                    transform = { p -> p?.char?.toString() ?: " " }
                )
            )
        }
        return rows.joinToString(
            prefix = "╭─" + "──┬─".repeat(maxX) + "──╮",
            separator = "\n├─" + "──┼─".repeat(maxX) + "──┤",
            postfix = "\n╰─" + "──┴─".repeat(maxX) + "──╯",
        )
    }
}

data class Node(
    val position: Position,
    val direction: CardinalDirection,
    val parent: Node? = null,
) {
    constructor(
        x: Int,
        y: Int,
        direction: CardinalDirection,
        parent: Node? = null,
    ) : this(Position(x, y), direction, parent)

    val path: List<Node> = parent?.let { parent.path + this } ?: listOf(this)


    val l: Node // left
        get() = Node(
            position.x + direction.x,
            position.y + direction.y,
            CardinalDirection.values()[(CardinalDirection.values().size + direction.ordinal - 1) % CardinalDirection.values().size],
            this
        )
    val f: Node // forward
        get() = Node(position.x + direction.x, position.y + direction.y, direction, this)
    val r: Node //right
        get() = Node(
            position.x + direction.x,
            position.y + direction.y,
            CardinalDirection.values()[(direction.ordinal + 1) % CardinalDirection.values().size],
            this
        )

    val length: Int = path.size

    override fun toString(): String = parent?.let { parent.toString() + direction.toString() } ?: direction.toString()

}


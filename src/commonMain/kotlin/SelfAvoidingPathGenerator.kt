import kotlin.math.abs

private val Int.isOdd: Boolean get() = this % 2 == 1
private val Int.isEven: Boolean get() = !this.isOdd

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

class SelfAvoidingPathGenerator(
    val boundary: Boundary,
    val length: Int,
    val start: Position = boundary.a,
    val end: Position = boundary.b,
    val startDirection: CardinalDirection = CardinalDirection.EAST,
    val endDirection: CardinalDirection = CardinalDirection.EAST
) {

    val nodesToExplore = mutableListOf<Node>()
    val solutions = mutableListOf<Node>()
    val illegalNodePredicates: List<(Node) -> Boolean> // if at least one filter matches, node is illegal
    val validSolutionPredicates: List<(Node) -> Boolean> // if ALL filter matches, node is a solution

    init {
        if (boundary.width < 2 || boundary.height < 2) throw Error("Board size has to be at least 2x2")
        if (start !in boundary) throw IllegalArgumentException("Start has to be within boundary")
        if (end !in boundary) throw IllegalArgumentException("End has to be within boundary")
        if (Boundary(start, end).let {
                length < it.width + it.height - 1
            }) throw IllegalArgumentException("Requested path length is shorter than shortest possible path from start to end")
        if (boundary.width * boundary.height < length) throw IllegalArgumentException("Requested path does not fit within the boundary")
        if (Boundary(
                start,
                end
            ).let { it.width * it.height }.isOdd && length.isOdd
        ) throw IllegalArgumentException("Pretty sure solution doesn't exist :)")
        if (Boundary(
                start,
                end
            ).let { it.width * it.height }.isEven && length.isEven
        ) throw IllegalArgumentException("Pretty sure solution doesn't exist :)")

        nodesToExplore.add(Node(start, startDirection))
        illegalNodePredicates = listOf(
            // If node out of boundary, it is illegal, return true
            { n -> n.position !in boundary },
            // If node is overlapping another node from its path, it is illegal
            { n -> n.path.dropLast(1).find { it.position == n.position }?.let { true } ?: false },
            // If node is is too long, it is illegal
            { n -> n.length > length },
            // If node is is too far from end, it is illegal
            { n ->
                Boundary(n.position, end).let {
                    length < it.width + it.height - 1
                }
            },
        )
        validSolutionPredicates = listOf(
            // Node has to be in the right position
            { n -> n.position == end },
            // Node has to be in the right direction
            { n -> n.direction == endDirection },
            // Node has to have correct length
            { n -> n.length == length },
        )
    }

    val solution: Iterator<Node> = object : Iterator<Node> {

        val preCalculatedSolutions = mutableListOf<Node>()

        override fun hasNext(): Boolean {
            if (preCalculatedSolutions.isNotEmpty()) return true
            while (nodesToExplore.isNotEmpty()) {
                val n = nodesToExplore.removeLastOrNull() ?: return false // no more steps to take
                var foundSolution = false
                listOf(n.l, n.f, n.r).shuffled().map { node ->
                    if (illegalNodePredicates.none { it(node) }) { // if each generated node legal
                        if (validSolutionPredicates.all { it(node) }) { // if valid node is a solution
                            preCalculatedSolutions.add(node)
                            foundSolution = true
                        } else {
                            nodesToExplore.add(node)
                        }
                    }
                }
                if (foundSolution) return true
            }
            return false
        }

        override fun next(): Node {
            preCalculatedSolutions.removeFirstOrNull()?.let {
                solutions.add(it)
                return it
            } ?: kotlin.run { }
            throw UnsupportedOperationException("No precalculated solution available, have you called hasNext() first?")
        }
    }
}

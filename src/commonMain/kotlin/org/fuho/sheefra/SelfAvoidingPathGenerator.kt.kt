package org.fuho.sheefra

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
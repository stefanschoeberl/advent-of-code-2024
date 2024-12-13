package dev.ssch.day12

import java.io.File

data class Position(
    val row: Int,
    val column: Int,
) {
    fun up(): Position {
        return Position(row - 1, column)
    }

    fun down(): Position {
        return Position(row + 1, column)
    }

    fun left(): Position {
        return Position(row, column - 1)
    }

    fun right(): Position {
        return Position(row, column + 1)
    }

    fun neighbours(): Set<Position> {
        return setOf(up(), down(), left(), right())
    }
}

typealias Map = List<String>

fun Map.getLabel(position: Position): Char {
    return this[position.row][position.column]
}

fun Map.inBounds(position: Position): Boolean {
    return position.row in this.indices && position.column in this[0].indices
}

val Map.positionsWithLabel: Sequence<Pair<Position, Char>>
    get() = this.asSequence().flatMapIndexed { row, line ->
        line.asSequence().mapIndexed { col, c -> Pair(Position(row, col), c) }
    }


fun Map.floodFill(startPosition: Position): Set<Position> {
    val currentLabel = this.getLabel(startPosition)

    data class State(
        val positionsToVisit: Set<Position>,
        val visitedPositions: Set<Position>,
    )

    return generateSequence(State(setOf(startPosition), emptySet())) { (positionsToVisit, visitedPositions) ->
        val currentPosition = positionsToVisit.first()
        val nextPositions = currentPosition.neighbours().filter {
            inBounds(it) && it !in visitedPositions && getLabel(it) == currentLabel
        }
        State(
            positionsToVisit - currentPosition + nextPositions,
            visitedPositions + currentPosition,
        )
    }.first { it.positionsToVisit.isEmpty() }.visitedPositions
}

fun computePerimeter(positions: Set<Position>): Int {
    return positions.sumOf {
        it.neighbours().count { neighbour -> neighbour !in positions }
    }
}

private fun computeRegions(
    allPositions: Set<Position>,
    map: Map
): List<Set<Position>> {
    data class State(
        val availablePositions: Set<Position>,
        val segments: List<Set<Position>>
    )

    return generateSequence(State(allPositions, emptyList())) { (availablePositions, segments) ->
        val segmentPositions = map.floodFill(availablePositions.first())
        State(
            availablePositions - segmentPositions,
            segments.plusElement(segmentPositions)
        )
    }.first { it.availablePositions.isEmpty() }.segments
}

fun part1(file: File) {
    val map = file.readLines()

    val allPositions = map.positionsWithLabel.map { it.first }.toSet()
    val segments = computeRegions(allPositions, map)

    val result = segments.sumOf {
        it.size * computePerimeter(it)
    }
    println(result)
}

// ---

enum class Direction {
    Horizontal,
    Vertical
}

enum class Side {
    Left,
    Right,
}

data class Edge(
    val start: Position,
    val end: Position,
    val inside: Side
) {
    val direction: Direction
        get() = if (start.row == end.row) Direction.Horizontal else Direction.Vertical
}

data class NeighbourDescription(
    val neighbourPosition: (Position) -> Position,
    val startPosition: (Position) -> Position,
    val endPosition: (Position) -> Position,
    val inside: Side,
)

fun computeNumberOfSides(positions: Set<Position>): Int {
    val neighbourDescriptions = listOf(
        NeighbourDescription(Position::up, { it }, { it.right() }, Side.Right),
        NeighbourDescription(Position::down, { it.down() }, { it.down().right() }, Side.Left),
        NeighbourDescription(Position::right, { it.right() }, { it.down().right() }, Side.Right),
        NeighbourDescription(Position::left, { it }, { it.down() }, Side.Left),
    )

    val edges = positions.flatMap { currentPosition ->
        neighbourDescriptions.asSequence().filter {
            it.neighbourPosition(currentPosition) !in positions
        }.map {
            Edge(it.startPosition(currentPosition), it.endPosition(currentPosition), it.inside)
        }
    }.toSet()

    fun canMerge(firstEdge: Edge, secondEdge: Edge): Boolean {
        return firstEdge.direction == secondEdge.direction
                && firstEdge.end == secondEdge.start
                && firstEdge.inside == secondEdge.inside
    }

    val mergedEdges = generateSequence(edges) { currentEdges ->
        val edgesToMerge = currentEdges
            .firstNotNullOfOrNull { firstEdge ->
                currentEdges.find { secondEdge -> canMerge(firstEdge, secondEdge) }
                    ?.let {
                        Pair(firstEdge, it)
                    }
            }
        edgesToMerge?.let { (first, second) ->
            currentEdges - first - second + Edge(first.start, second.end, first.inside)
        }
    }.last()

    return mergedEdges.size
}

fun part2(file: File) {
    val map = file.readLines()

    val allPositions = map.positionsWithLabel.map { it.first }.toSet()
    val segments = computeRegions(allPositions, map)

    val result = segments.sumOf {
        it.size * computeNumberOfSides(it)
    }
    println(result)
}


fun main() {
    part1(File("inputs/12-part1.txt"))
    part1(File("inputs/12.txt"))
    println("---")
    part2(File("inputs/12-part1.txt"))
    part2(File("inputs/12-part2-1.txt"))
    part2(File("inputs/12-part2-2.txt"))
    part2(File("inputs/12.txt"))
}

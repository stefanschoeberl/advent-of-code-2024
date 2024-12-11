package dev.ssch.day10

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

fun Map.getHeight(position: Position): Int {
    return this[position.row][position.column].digitToInt()
}

fun Map.inBounds(position: Position): Boolean {
    return position.row in this.indices && position.column in this[0].indices
}

val Map.positionsWithHeights: Sequence<Pair<Position, Int>>
    get() = this.asSequence().flatMapIndexed { row, line ->
        line.asSequence().mapIndexed { col, c -> Pair(Position(row, col), c.digitToInt()) }
    }

fun computeStartingPositions(map: List<String>): List<Position> {
    return map.positionsWithHeights
        .filter { (_, height) -> height == 0 }
        .map { (position, _) -> position }
        .toList()
}

fun part1(file: File) {
    val map = file.readLines()

    val startingPositions = computeStartingPositions(map)

    fun computeReachableTrailheads(position: Position): Set<Position> {
        val currentHeight = map.getHeight(position)
        return if (currentHeight == 9) {
            setOf(position)
        } else {
            position.neighbours()
                .filter { map.inBounds(it) }
                .filter { map.getHeight(it) == currentHeight + 1 }
                .flatMap { computeReachableTrailheads(it) }
                .toSet()
        }
    }

    val result = startingPositions.sumOf { computeReachableTrailheads(it).size }
    println(result)
}

// ---

fun part2(file: File) {
    val map = file.readLines()

    val startingPositions = computeStartingPositions(map)

    fun computeNumberOfDistinctTrails(position: Position): Int {
        val currentHeight = map.getHeight(position)
        return if (currentHeight == 9) {
            1
        } else {
            position.neighbours()
                .filter { map.inBounds(it) }
                .filter { map.getHeight(it) == currentHeight + 1 }
                .sumOf { computeNumberOfDistinctTrails(it) }
        }
    }

    val result = startingPositions.sumOf { computeNumberOfDistinctTrails(it) }
    println(result)
}

fun main() {
    part1(File("inputs/10-part1.txt"))
    part1(File("inputs/10.txt"))
    println("---")
    part2(File("inputs/10-part1.txt"))
    part2(File("inputs/10.txt"))
}

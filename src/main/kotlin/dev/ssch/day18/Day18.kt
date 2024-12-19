package dev.ssch.day18

import dev.ssch.day12.Position
import java.io.File

fun shortestPath(start: Position, end: Position, corruptedPositions: Set<Position>, maximumMemoryCoordinate: Int): Int? {
    val openPositions = mutableSetOf(start)
    val closedPositions = mutableSetOf<Position>()
    val score = mutableMapOf(start to 0).withDefault { Int.MAX_VALUE }

    while (openPositions.isNotEmpty()) {
        val currentPosition = openPositions.minByOrNull { score.getValue(it) }!!
        openPositions.remove(currentPosition)
        if (currentPosition !in closedPositions) {
            closedPositions.add(currentPosition)

            val currentScore = score.getValue(currentPosition)

            currentPosition.neighbours()
                .filter { it !in corruptedPositions && it.row in 0..maximumMemoryCoordinate && it.column in 0..maximumMemoryCoordinate }
                .filter { currentScore + 1 < score.getValue(it) }
                .forEach { position ->
                    openPositions.add(position)
                    score[position] = currentScore + 1
                }
        }
    }
    return score[end]
}

fun part1(file: File, maximumMemoryCoordinate: Int, numberOfCorruptedBytes: Int) {
    val lines = file.readLines()

    val corruptedPositions = lines.take(numberOfCorruptedBytes).map { line ->
        val (column, row) = line.split(",").map { it.toInt() }
        Position(row, column)
    }.toSet()
    val start = Position(0, 0)
    val end = Position(maximumMemoryCoordinate, maximumMemoryCoordinate)

    println(shortestPath(start, end, corruptedPositions, maximumMemoryCoordinate))
}

// ---

fun part2(file: File, maximumMemoryCoordinate: Int) {
    val lines = file.readLines()

    val corruptedPositions = lines.map { line ->
        val (column, row) = line.split(",").map { it.toInt() }
        Position(row, column)
    }.toList()
    val start = Position(0, 0)
    val end = Position(maximumMemoryCoordinate, maximumMemoryCoordinate)

    val cutOffPosition = (1..corruptedPositions.size).asSequence().mapNotNull { numberOfCorruptedPositions ->
        if (shortestPath(start, end, corruptedPositions.take(numberOfCorruptedPositions).toSet(), maximumMemoryCoordinate) == null) {
            corruptedPositions[numberOfCorruptedPositions - 1]
        } else {
            null
        }
    }.first()
    println("${cutOffPosition.column},${cutOffPosition.row}")
}

fun main() {
    part1(File("inputs/18-part1.txt"), 6, 12)
    part1(File("inputs/18.txt"), 70, 1024)
    println("---")
    part2(File("inputs/18-part1.txt"), 6)
    part2(File("inputs/18.txt"), 70)
}

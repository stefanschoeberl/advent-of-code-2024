package dev.ssch.day21

import dev.ssch.day12.Position
import java.io.File
import kotlin.math.abs

typealias CoordinateMap = Map<Char, Position>

fun List<String>.toCoordinateMap(): CoordinateMap {
    return flatMapIndexed { row, line ->
        line.mapIndexed { col, char -> Pair(char, Position(row, col)) }
    }.toMap()
}

val keyPad = listOf(
    "789",
    "456",
    "123",
    "_0A"
).toCoordinateMap()

val movementPad = listOf(
    "_^A",
    "<v>"
).toCoordinateMap()

operator fun Position.minus(other: Position): Position {
    return Position(row - other.row, column - other.column)
}

operator fun Position.plus(other: Position): Position {
    return Position(row + other.row, column + other.column)
}

fun Position.manhattanAbs(): Int {
    return abs(row) + abs(column)
}

fun Position.manhattanAbsTo(other: Position): Int {
    return (other - this).manhattanAbs()
}

fun generateMoveSequencePossibilities(from: Position, to: Position, emptyButtonPosition: Position): Set<String> {
    return if (from == to) {
        setOf("")
    } else {
        val distance = from.manhattanAbsTo(to)
        sequenceOf(
            Pair(Position(1, 0), 'v'),
            Pair(Position(-1, 0), '^'),
            Pair(Position(0, 1), '>'),
            Pair(Position(0, -1), '<'),
        ).map {
            Pair(it.first + from, it.second)
        }.filter { (destination, _) ->
            destination != emptyButtonPosition && destination.manhattanAbsTo(to) < distance
        }.flatMap { (destination, move) ->
            generateMoveSequencePossibilities(destination, to, emptyButtonPosition).map { move + it }
        }.toSet()
    }
}

fun generateMoveSequencePossibilities(from: Char, to: Char, coordinateMap: CoordinateMap): Set<String> {
    val fromPosition = coordinateMap[from]!!
    val toPosition = coordinateMap[to]!!
    val emptyButtonPosition = coordinateMap['_']!!
    return generateMoveSequencePossibilities(fromPosition, toPosition, emptyButtonPosition)
}

val computeMinMovesLengthCache = mutableMapOf<Triple<String, CoordinateMap, Int>, Long>()
fun computeMinMovesLength(sequence: String, coordinateMap: CoordinateMap, depth: Int): Long {
    fun compute(): Long {
        if (depth == 0) {
            return sequence.length.toLong()
        }

        val options = "A$sequence".windowed(2).map {
            generateMoveSequencePossibilities(it[0], it[1], coordinateMap).map { moveSequence ->
                computeMinMovesLength(moveSequence + "A", movementPad, depth - 1)
            }.toSet()
        }

        fun findBestCombination(combinationSoFar: Long, currentOptionsIndex: Int): Long {
            return if (currentOptionsIndex !in options.indices) {
                combinationSoFar
            } else {
                options[currentOptionsIndex].map {
                    findBestCombination(combinationSoFar + it, currentOptionsIndex + 1)
                }.minOf { it }
            }
        }

        return findBestCombination(0, 0)
    }

    val key = Triple(sequence, coordinateMap, depth)
    return computeMinMovesLengthCache[key] ?: compute().also { computeMinMovesLengthCache[key] = it }
}

fun solve(file: File, numberOfRobots: Int) {
    val codes = file.readLines()
    val result = codes.sumOf { code ->
        computeMinMovesLength(code, keyPad, numberOfRobots + 1) * code.filter { it.isDigit() }.toInt()
    }
    println(result)
}

fun main() {
    solve(File("inputs/21-part1.txt"), 2)
    solve(File("inputs/21.txt"), 2)
    println("---")
    solve(File("inputs/21-part1.txt"), 25)
    solve(File("inputs/21.txt"), 25)
}

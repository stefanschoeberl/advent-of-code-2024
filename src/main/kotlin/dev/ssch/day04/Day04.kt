package dev.ssch.day04

import java.io.File

fun <T> List<List<T>>.transposeListOfLists(): List<List<T>> {
    return this[0].indices.map { i -> this.indices.map { j -> this[j][i] } }
}

fun List<String>.transposeListOfString(): List<String> {
    return this.map { it.toList() }.transposeListOfLists().map { it.joinToString("") }
}

fun part1(file: File) {
    val lines = file.readLines()

    val pattern = Regex("(?=(XMAS|SAMX))")

    fun List<String>.countMatches(): Int {
        return this.sumOf { line -> pattern.findAll(line).count() }
    }

    val horizontalMatches = lines.countMatches()
    val verticalMatches = lines.transposeListOfString().countMatches()
    val firstDiagonalMatches = lines.mapIndexed { index, line ->
        "${" ".repeat(index)}$line${" ".repeat(lines.size - index)}"
    }.transposeListOfString().countMatches()
    val secondDiagonalMatches = lines.mapIndexed { index, line ->
        "${" ".repeat(lines.size - index)}$line${" ".repeat(index)}"
    }.transposeListOfString().countMatches()

    println(horizontalMatches + verticalMatches + firstDiagonalMatches + secondDiagonalMatches)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()

    val pattern = Regex("(?=(MAS|SAM))")

    fun List<String>.findMatches(): List<Pair<Int, Int>> {
        return this.flatMapIndexed { row, s ->
            pattern.findAll(s).map { Pair(row, it.range.first + 1) }
        }
    }

    val shiftedForFirstDiagonal = lines.mapIndexed { index, line ->
        "${" ".repeat(index)}$line${" ".repeat(lines.size - index)}"
    }

    val firstDiagonalMatches = shiftedForFirstDiagonal.transposeListOfString().findMatches()
        .map { Pair(it.second, it.first - it.second) }

    val shiftedForSecondDiagonal = lines.mapIndexed { index, line ->
        "${" ".repeat(lines.size - index)}$line${" ".repeat(index)}"
    }

    val secondDiagonalMatches = shiftedForSecondDiagonal.transposeListOfString().findMatches()
        .map { Pair(it.second, it.second - (lines.size - it.first)) }

    val intersectingMatches = firstDiagonalMatches.intersect(secondDiagonalMatches)

    println(intersectingMatches.size)
}

fun main() {
    part1(File("inputs/04-part1.txt"))
    part1(File("inputs/04.txt"))
    println("---")
    part2(File("inputs/04-part1.txt"))
    part2(File("inputs/04.txt"))
}

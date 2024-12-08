package dev.ssch.day08

import java.io.File

data class Point(
    val row: Int,
    val col: Int,
) {
    operator fun minus(other: Point): Point {
        return Point(row - other.row, col - other.col)
    }

    operator fun plus(other: Point): Point {
        return Point(row + other.row, col + other.col)
    }

    operator fun times(scalar: Int): Point {
        return Point(row * scalar, col * scalar)
    }

    operator fun div(scalar: Int): Point {
        return Point(row / scalar, col / scalar)
    }
}

data class Antenna(
    val position: Point,
    val frequency: Char,
)

fun parseLines(lines: List<String>): List<Antenna> {
    return lines.flatMapIndexed { row: Int, line: String ->
        line.mapIndexedNotNull { col, c ->
            if (c != '.') Antenna(Point(row, col), c) else null
        }
    }
}

fun computeAntinodes(antennas: List<Antenna>, rowRange: IntRange, colRange: IntRange): List<Point> {
    val pairs = antennas.flatMapIndexed { index, first ->
        antennas.drop(index + 1).map { second -> Pair(first.position, second.position) }
    }

    fun computeAntinode(first: Point, second: Point): Point {
        return (second - first) * 2 + first
    }

    return pairs.flatMap { (first, second) ->
        listOf(computeAntinode(first, second), computeAntinode(second, first))
    }.filter { it.row in rowRange && it.col in colRange }
}

fun part1(file: File) {
    val lines = file.readLines()
    val antennas = parseLines(lines)

    val antinodes = antennas.groupBy { it.frequency }
        .map { it.value }
        .flatMap { computeAntinodes(it, lines.indices, lines[0].indices) }
        .toSet()

    println(antinodes.size)
}

// ---

fun computeAntinodesWithResonance(antennas: List<Antenna>, rowRange: IntRange, colRange: IntRange): List<Point> {
    val pairs = antennas.flatMapIndexed { index, first ->
        antennas.drop(index + 1).map { second -> Pair(first.position, second.position) }
    }

    fun computeAntinodes(first: Point, second: Point): List<Point> {
        val directionVector = (second - first).let { it / (it.row.toBigInteger().gcd(it.col.toBigInteger()).toInt()) }
        return generateSequence(1) { it + 1 }
            .map { directionVector * it + first }
            .takeWhile { it.row in rowRange && it.col in colRange }
            .toList()
    }

    return pairs.flatMap { (first, second) ->
        computeAntinodes(first, second) + computeAntinodes(second, first)
    }
}

fun part2(file: File) {
    val lines = file.readLines()
    val antennas = parseLines(lines)

    val antinodes = antennas.groupBy { it.frequency }
        .map { it.value }
        .flatMap { computeAntinodesWithResonance(it, lines.indices, lines[0].indices) }
        .toSet()

    println(antinodes.size)
}

fun main() {
    part1(File("inputs/08-part1.txt"))
    part1(File("inputs/08.txt"))
    println("---")
    part2(File("inputs/08-part1.txt"))
    part2(File("inputs/08.txt"))
}

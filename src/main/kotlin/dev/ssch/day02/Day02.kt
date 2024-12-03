package dev.ssch.day02

import java.io.File
import kotlin.math.abs

fun isSafe(levels: List<Int>): Boolean {
    val pairs = levels.windowed(2)
    return (pairs.all { it[0] <= it[1] }
            || pairs.all { it[0] >= it[1] })
            && pairs.all { abs(it[0] - it[1]) in 1..3 }
}

fun part1(file: File) {
    val lines = file.readLines()

    val safeReports = lines
        .map { it.split(" ").map { it.toInt() } }
        .count { isSafe(it) }

    println(safeReports)
}

// ---

fun isSafeWithToleration(levels: List<Int>): Boolean {
    return isSafe(levels) || levels.indices.asSequence().map {
        levels.filterIndexed { index, _ -> index != it }
    }.any { isSafe(it) }
}

fun part2(file: File) {
    val lines = file.readLines()

    val safeReports = lines
        .map { it.split(" ").map { it.toInt() } }
        .count { isSafeWithToleration(it) }

    println(safeReports)
}

fun main() {
    part1(File("inputs/02-part1.txt"))
    part1(File("inputs/02.txt"))
    println("---")
    part2(File("inputs/02-part1.txt"))
    part2(File("inputs/02.txt"))
}

package dev.ssch.day25

import dev.ssch.day04.transposeListOfString
import dev.ssch.day05.split
import java.io.File

fun part1(file: File) {
    val patterns = file.readLines().split { it.isEmpty() }

    val (locks, keys) = patterns.map { pattern ->
        Pair(pattern.transposeListOfString().map { it.count { it == '#' } - 1 }, pattern[0][0] == '#')
    }.groupBy({ it.second }, { it.first }).let {
        Pair(it[true]!!, it[false]!!)
    }

    val height = patterns.first().size - 2

    val result = locks.sumOf { lock ->
        keys.count { key ->
            lock.zip(key).all { (l, k) -> l <= height - k }
        }
    }

    println(result)
}

// ---

fun main() {
    part1(File("inputs/25-part1.txt"))
    part1(File("inputs/25.txt"))
    println("---")
}

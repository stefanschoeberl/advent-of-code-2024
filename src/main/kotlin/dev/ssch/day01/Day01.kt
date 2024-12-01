package dev.ssch.day01

import java.io.File
import kotlin.math.abs

fun part1(file: File) {
    val lines = file.readLines()

    val pairs = lines.map {
        it.split(" ").filter { it.isNotBlank() }.map { it.toInt() }
    }

    val list1 = pairs.map { it[0] }.sorted()
    val list2 = pairs.map { it[1] }.sorted()

    val result = list1.zip(list2)
        .map { abs(it.first - it.second) }
        .sum()
    println(result)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()

    val pairs = lines.map {
        it.split(" ").filter { it.isNotBlank() }.map { it.toInt() }
    }

    val list1 = pairs.map { it[0] }.sorted()
    val list2 = pairs.map { it[1] }.sorted()

    val result = list1
        .map { numberInLeft -> numberInLeft * list2.count { it == numberInLeft } }
        .sum()

    println(result)
}

fun main() {
    part1(File("inputs/01-part1.txt"))
    part1(File("inputs/01.txt"))
    println("---")
    part2(File("inputs/01-part1.txt"))
    part2(File("inputs/01.txt"))
}

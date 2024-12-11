package dev.ssch.day11

import java.io.File

fun Long.splitStone(): List<Long> {
    val text = this.toString()
    return listOf(
        text.take(text.length / 2).toLong(),
        text.drop(text.length / 2).toLong(),
    )
}

fun Long.hasEvenNumberOfDigits() = this.toString().length % 2 == 0

fun computeNumberOfStonesAfterBlinks(numberOnStone: Long, numberOfBlinks: Int): Int {
    return if (numberOfBlinks == 0) {
        1
    } else if (numberOnStone == 0L) {
        computeNumberOfStonesAfterBlinks(1, numberOfBlinks - 1)
    } else if (numberOnStone.hasEvenNumberOfDigits()) {
        numberOnStone
            .splitStone()
            .sumOf { computeNumberOfStonesAfterBlinks(it, numberOfBlinks - 1) }
    } else {
        computeNumberOfStonesAfterBlinks(numberOnStone * 2024, numberOfBlinks - 1)
    }
}

fun part1(file: File) {
    val line = file.readLines()[0]
    val stones = line.split(" ").map { it.toLong() }
    val result = stones.sumOf { computeNumberOfStonesAfterBlinks(it, 25) }
    println(result)
}

// ---

val cache = mutableMapOf<Pair<Long, Int>, Long>()
fun computeNumberOfStonesAfterBlinksWithCache(numberOnStone: Long, numberOfBlinks: Int): Long {

    fun compute(): Long {
        return if (numberOfBlinks == 0) {
            1
        } else if (numberOnStone == 0L) {
            computeNumberOfStonesAfterBlinksWithCache(1, numberOfBlinks - 1)
        } else if (numberOnStone.hasEvenNumberOfDigits()) {
            numberOnStone
                .splitStone()
                .sumOf { computeNumberOfStonesAfterBlinksWithCache(it, numberOfBlinks - 1) }
        } else {
            computeNumberOfStonesAfterBlinksWithCache(numberOnStone * 2024, numberOfBlinks - 1)
        }
    }

    val key = Pair(numberOnStone, numberOfBlinks)
    return cache[key] ?: compute().also { cache[key] = it }
}

fun part2(file: File) {
    val line = file.readLines()[0]
    val stones = line.split(" ").map { it.toLong() }
    val result = stones.sumOf { computeNumberOfStonesAfterBlinksWithCache(it, 75) }
    println(result)
}

fun main() {
    part1(File("inputs/11-part1.txt"))
    part1(File("inputs/11.txt"))
    println("---")
    part2(File("inputs/11.txt"))
}

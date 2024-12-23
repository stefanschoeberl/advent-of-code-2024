package dev.ssch.day22

import java.io.File

private fun secretNumberSequence(seed: Long): Sequence<Long> {
    return generateSequence(seed) {
        val afterStep1 = (it xor (it * 64)) % 16777216
        val afterStep2 = (afterStep1 xor (afterStep1 / 32)) % 16777216
        (afterStep2 xor (afterStep2 * 2048)) % 16777216
    }
}

fun generateNthNumber(seed: Long, n: Int): Long {
    return secretNumberSequence(seed).drop(n).first()
}

fun part1(file: File) {
    val seeds = file.readLines().map { it.toLong() }
    val result = seeds.sumOf {
        generateNthNumber(it, 2000)
    }
    println(result)
}

// ---

fun part2(file: File) {
    val seeds = file.readLines().map { it.toLong() }

    val numberOfBananasForChangeSequence = seeds.map { seed ->
        secretNumberSequence(seed)
            .map { it % 10 }
            .take(2001)
            .windowed(2)
            .map { (oldPrice, currentPrice) -> Pair(currentPrice - oldPrice, currentPrice) }
            .windowed(4)
            .toList()
            .reversed()
            .associateBy(
                { changeSequenceWithPrice -> changeSequenceWithPrice.map { it.first } },
                { changeSequenceWithPrice -> changeSequenceWithPrice.last().second }
            )
    }

    val allChangeSequences = numberOfBananasForChangeSequence.flatMap { it.keys }.toSet()

    val result = allChangeSequences.maxOf { changeSequence ->
        numberOfBananasForChangeSequence.mapNotNull { it[changeSequence] }.sum()
    }
    println(result)
}


fun main() {
    part1(File("inputs/22-part1.txt"))
    part1(File("inputs/22.txt"))
    println("---")
    part2(File("inputs/22-part2.txt"))
    part2(File("inputs/22.txt"))
}

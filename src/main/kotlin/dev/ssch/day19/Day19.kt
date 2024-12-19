package dev.ssch.day19

import dev.ssch.day05.split
import java.io.File

fun part1(file: File) {
    val (towelLine, designs) = file.readLines().split { it.isEmpty() }
    val towelTypes = towelLine.first().split(", ")

    val cache = mutableMapOf<String, Boolean>()
    fun isPossible(pattern: String): Boolean {
        fun compute(): Boolean {
            return pattern.isEmpty() ||
                    towelTypes.any { towel ->
                        pattern.startsWith(towel) && isPossible(pattern.drop(towel.length))
                    }
        }
        return cache[pattern] ?: compute().also { cache[pattern] = it }
    }

    val result = designs.count { isPossible(it) }
    println(result)
}

// ---

fun part2(file: File) {
    val (towelLine, designs) = file.readLines().split { it.isEmpty() }
    val towelTypes = towelLine.first().split(", ")

    val cache = mutableMapOf<String, Long>()
    fun numberOfArrangements(pattern: String): Long {
        fun compute(): Long {
            return if (pattern.isEmpty()) {
                1
            } else {
                towelTypes
                    .filter { towel -> pattern.startsWith(towel) }
                    .sumOf { numberOfArrangements(pattern.drop(it.length)) }
            }
        }
        return cache[pattern] ?: compute().also { cache[pattern] = it }
    }

    val result = designs.sumOf { numberOfArrangements(it) }
    println(result)
}

fun main() {
    part1(File("inputs/19-part1.txt"))
    part1(File("inputs/19.txt"))
    println("---")
    part2(File("inputs/19-part1.txt"))
    part2(File("inputs/19.txt"))
}

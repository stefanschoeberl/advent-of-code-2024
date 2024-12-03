package dev.ssch.day03

import java.io.File
import kotlin.math.abs

fun part1(file: File) {
    val lines = file.readLines()

    val pattern = Regex("mul\\(([0-9]+),([0-9]+)\\)")
    val result = lines.flatMap {
        pattern.findAll(it)
    }.sumOf {
        it.groupValues[1].toInt() * abs(it.groupValues[2].toInt())
    }

    println(result)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()

    data class State(val remainingResults: List<MatchResult>, val enabled: Boolean) {
        fun enable(): State {
            return State(remainingResults, true)
        }

        fun disable(): State {
            return State(remainingResults, false)
        }

        fun addMatchResultIfEnabled(matchResult: MatchResult): State {
            return if (enabled) {
                State(remainingResults + matchResult, enabled)
            } else {
                this
            }
        }
    }

    val pattern = Regex("mul\\(([0-9]+),([0-9]+)\\)|do\\(\\)|don't\\(\\)")
    val result = lines.flatMap {
        pattern.findAll(it)
    }.fold(State(listOf(), true)) { state, matchResult ->
        when (matchResult.value) {
            "do()" -> state.enable()
            "don't()" -> state.disable()
            else -> state.addMatchResultIfEnabled(matchResult)
        }
    }.remainingResults.sumOf {
        it.groupValues[1].toInt() * abs(it.groupValues[2].toInt())
    }

    println(result)
}

fun main() {
    part1(File("inputs/03-part1.txt"))
    part1(File("inputs/03.txt"))
    println("---")
    part2(File("inputs/03-part2.txt"))
    part2(File("inputs/03.txt"))
}

package dev.ssch.day07

import java.io.File

data class CalibrationChallenge(
    val total: Long,
    val numbers: List<Long>,
)

fun parseChallenge(it: String): CalibrationChallenge {
    val (totalText, remainingText) = it.split(":")
    val numbers = remainingText.split(" ").filter { it.isNotEmpty() }.map { it.toLong() }
    return CalibrationChallenge(totalText.toLong(), numbers)
}

fun isSolvable(total: Long, numbers: List<Long>): Boolean {
    return if (numbers.isEmpty()) {
        false
    } else if (numbers.size == 1) {
        total == numbers.first()
    } else {
        val lastNumber = numbers.last()
        val remainingNumbers = numbers.dropLast(1)
        isSolvable(total - lastNumber, remainingNumbers) ||
                (lastNumber != 0L && total % lastNumber == 0L && isSolvable(total.div(lastNumber), remainingNumbers))
    }
}

fun part1(file: File) {
    val lines = file.readLines()

    val challenges = lines.map { parseChallenge(it) }

    val result = challenges
        .filter { isSolvable(it.total, it.numbers) }
        .sumOf { it.total }

    println(result)
}

// ---

fun isSolvableWithAdditionalOperator(total: Long, numbers: List<Long>): Boolean {
    return if (numbers.isEmpty()) {
        false
    } else if (numbers.size == 1) {
        total == numbers.first()
    } else {
        val lastNumber = numbers.last()
        val remainingNumbers = numbers.dropLast(1)

        fun checkAddition(): Boolean {
            return isSolvableWithAdditionalOperator(total - lastNumber, remainingNumbers)
        }

        fun checkMultiplication(): Boolean {
            return (lastNumber != 0L
                    && total % lastNumber == 0L
                    && isSolvableWithAdditionalOperator(total.div(lastNumber), remainingNumbers))
        }

        fun checkConcat(): Boolean {
            val totalAsString = total.toString()
            val lastNumberAsString = lastNumber.toString()
            val remainingOfTotal = totalAsString.dropLast(lastNumberAsString.length)
            return totalAsString.endsWith(lastNumberAsString)
                    && isSolvableWithAdditionalOperator(remainingOfTotal.toLong(), remainingNumbers)
        }

        checkAddition() || checkMultiplication() || checkConcat()
    }
}

fun part2(file: File) {
    val lines = file.readLines()

    val challenges = lines.map { parseChallenge(it) }

    val result = challenges
        .filter { isSolvableWithAdditionalOperator(it.total, it.numbers) }
        .sumOf { it.total }

    println(result)
}

fun main() {
    part1(File("inputs/07-part1.txt"))
    part1(File("inputs/07.txt"))
    println("---")
    part2(File("inputs/07-part1.txt"))
    part2(File("inputs/07.txt"))
}

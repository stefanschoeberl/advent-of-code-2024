package dev.ssch.day13

import dev.ssch.day05.split
import java.io.File

data class Fraction(
    val numerator: Long,
    val denominator: Long
) {

    operator fun plus(that: Fraction): Fraction {
        return Fraction(
            numerator * that.denominator + denominator * that.numerator,
            denominator * that.denominator
        ).simplify()
    }

    operator fun minus(that: Fraction): Fraction {
        return Fraction(
            numerator * that.denominator - denominator * that.numerator,
            denominator * that.denominator
        ).simplify()
    }

    operator fun times(that: Fraction): Fraction {
        return Fraction(numerator * that.numerator, denominator * that.denominator).simplify()
    }

    operator fun div(that: Fraction): Fraction {
        return Fraction(numerator * that.denominator, denominator * that.numerator).simplify()
    }

    private fun simplify(): Fraction {
        val gcd = numerator.toBigInteger().gcd(denominator.toBigInteger()).toInt()
        val newNumerator = numerator / gcd
        val newDenominator = denominator / gcd
        return if (newDenominator < 0) {
            Fraction(-newNumerator, -newDenominator)
        } else {
            Fraction(newNumerator, newDenominator)
        }
    }

    fun isZero(): Boolean {
        return numerator == 0L
    }

    fun toLong(): Long? {
        return if (denominator == 1L) numerator else null
    }
}

fun Long.toFraction(): Fraction {
    return Fraction(this, 1)
}

data class Vector(val x: Fraction, val y: Fraction)

data class Machine(
    val a: Vector,
    val b: Vector,
    val p: Vector,
)

fun parseMachine(lines: List<String>): Machine {
    val buttonRegex = Regex("Button [AB]: X\\+([0-9]+), Y\\+([0-9]+)")
    val prizeRegex = Regex("Prize: X=([0-9]+), Y=([0-9]+)")

    fun lineToVector(line: String, regex: Regex): Vector {
        val (x, y) = regex.matchEntire(line)!!.destructured
        return Vector(x.toLong().toFraction(), y.toLong().toFraction())
    }

    return Machine(
        lineToVector(lines[0], buttonRegex),
        lineToVector(lines[1], buttonRegex),
        lineToVector(lines[2], prizeRegex),
    )
}

fun Machine.solve(): Pair<Long, Long>? {
    val factorForA = a.y - a.x * b.y / b.x
    val factorForB = b.y - b.x * a.y / a.x
    return if (!factorForA.isZero() && !factorForB.isZero()) {
        val pressesForA = ((p.y - p.x * b.y / b.x) / factorForA).toLong()
        val pressesForB = ((p.y - p.x * a.y / a.x) / factorForB).toLong()
        if (pressesForA != null && pressesForB != null) {
            Pair(pressesForA, pressesForB)
        } else {
            null
        }
    } else {
        null
    }
}

fun part1(file: File) {
    val lines = file.readLines()

    val machines = lines.split { it.isEmpty() }.map { parseMachine(it) }

    val result = machines
        .mapNotNull { it.solve() }
        .filter { (a, b) -> a >= 0 && b >= 0 }
        .sumOf { (a, b) -> 3 * a + b }
    println(result)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()

    val machines = lines.split { it.isEmpty() }.map { parseMachine(it) }
    val offset = 10000000000000.toFraction()

    val result = machines
        .map { (a, b, p) -> Machine(a, b, Vector(p.x + offset, p.y + offset)) }
        .mapNotNull { it.solve() }
        .filter { (a, b) -> a >= 0 && b >= 0 }
        .sumOf { (a, b) -> 3 * a + b }
    println(result)
}

fun main() {
    part1(File("inputs/13-part1.txt"))
    part1(File("inputs/13.txt"))
    println("---")
    part2(File("inputs/13-part1.txt"))
    part2(File("inputs/13.txt"))
}

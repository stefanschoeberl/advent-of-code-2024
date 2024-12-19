package dev.ssch.day17

import dev.ssch.day05.split
import java.io.File

fun parseRegisters(registersText: List<String>): List<Long> {
    val pattern = Regex("Register [A-C]: ([0-9]+)")
    return registersText.map {
        val (numberText) = pattern.find(it)!!.destructured
        numberText.toLong()
    }
}

enum class Instruction {
    Adv,
    Bxl,
    Bst,
    Jnz,
    Bxc,
    Out,
    Bdv,
    Cdv,
}

fun parseInstruction(operand: Int): Instruction {
    return when (operand) {
        0 -> Instruction.Adv
        1 -> Instruction.Bxl
        2 -> Instruction.Bst
        3 -> Instruction.Jnz
        4 -> Instruction.Bxc
        5 -> Instruction.Out
        6 -> Instruction.Bdv
        7 -> Instruction.Cdv
        else -> throw RuntimeException("Unknown operand: $operand")
    }
}

data class ComputerState(
    val a: Long,
    val b: Long,
    val c: Long,
    val ip: Int,
    val output: List<Int> = emptyList(),
) {
    private fun toCombo(operand: Int): Long {
        return when (operand) {
            in 0..3 -> operand.toLong()
            4 -> a
            5 -> b
            6 -> c
            else -> throw RuntimeException("Unknown operand: $operand")
        }
    }

    private infix fun Long.shrLong(bitCount: Long): Long {
        return this shr (bitCount and 0b11111111).toInt()
    }

    fun executeInstruction(instruction: Instruction, operand: Int): ComputerState {
        return when (instruction) {
            Instruction.Adv -> ComputerState(a shrLong toCombo(operand), b, c, ip + 2, output) // instead of a / (2.0.pow(operandValue).toInt())
            Instruction.Bxl -> ComputerState(a, b xor operand.toLong(), c, ip + 2, output)
            Instruction.Bst -> ComputerState(a, toCombo(operand) % 8, c, ip + 2, output)
            Instruction.Jnz -> ComputerState(a, b, c, if (a == 0L) ip + 2 else operand, output)
            Instruction.Bxc -> ComputerState(a, (b xor c), c, ip + 2, output)
            Instruction.Out -> ComputerState(a, b, c, ip + 2, output + (toCombo(operand) % 8).toInt())
            Instruction.Bdv -> ComputerState(a, a shrLong toCombo(operand), c, ip + 2, output)
            Instruction.Cdv -> ComputerState(a, b, a shrLong toCombo(operand), ip + 2, output)
        }
    }
}

fun parseProgram(programText: String): List<Int> {
    return programText.split(" ")[1].split(",").map { it.toInt() }
}

fun execute(program: List<Int>, initialState: ComputerState): Sequence<ComputerState> {
    return generateSequence(initialState) { state ->
        if (state.ip in program.indices) {
            val instruction = parseInstruction(program[state.ip])
            val operand = program[state.ip + 1]
            state.executeInstruction(instruction, operand)
        } else {
            null
        }
    }
}

fun part1(file: File) {
    val (registersText, programText) = file.readLines().split { it.isEmpty() }

    val (initialA, initialB, initialC) = parseRegisters(registersText)
    val program = parseProgram(programText[0])

    val endState = execute(program, ComputerState(initialA, initialB, initialC, 0)).last()

    println(endState.output.joinToString(",") { it.toString() })

}

// ---

fun part2Example(file: File) {
    val (registersText, programText) = file.readLines().split { it.isEmpty() }

    val (_, initialB, initialC) = parseRegisters(registersText)
    val program = parseProgram(programText[0])

    println((0..1000000000).firstOrNull { a ->
        execute(program, ComputerState(a.toLong(), initialB, initialC, 0)).last().output == program
    })
}

// ---

fun printProgram(file: File) {
    val (_, programText) = file.readLines().split { it.isEmpty() }
    val program = parseProgram(programText[0])

    program.chunked(2).forEachIndexed { index, (instruction, operand) ->
        fun toCombo(operand: Int): String {
            return when (operand) {
                in 0..3 -> operand.toString()
                4 -> "a"
                5 -> "b"
                6 -> "c"
                else -> throw RuntimeException("Unknown operand: $operand")
            }
        }

        val instructionString = when (parseInstruction(instruction)) {
            Instruction.Adv -> "a := a shr ${toCombo(operand)}"
            Instruction.Bxl -> "b := b xor $operand"
            Instruction.Bst -> "b := ${toCombo(operand)} % 8"
            Instruction.Jnz -> "Jnz(a) $operand"
            Instruction.Bxc -> "b := b xor c"
            Instruction.Out -> "Out(${toCombo(operand)} % 8)"
            Instruction.Bdv -> "b := a shr ${toCombo(operand)}"
            Instruction.Cdv -> "c := a shr ${toCombo(operand)}"
        }

        println("${(index * 2).toString().padStart(4)}: $instructionString")
    }
}

fun part2(file: File) {
    val (registersText, programText) = file.readLines().split { it.isEmpty() }

    val (_, initialB, initialC) = parseRegisters(registersText)
    val program = parseProgram(programText[0])

    fun canGenerateNumber(initialA: Int, numberToGenerate: Int): Boolean {
        val finalState = execute(program, ComputerState(initialA.toLong(), initialB, initialC, 0))
            .first { it.output.isNotEmpty() }

        return finalState.output.first() == numberToGenerate
    }

    val candidates = program.fold(emptySet<List<Int>>()) { candidates, numberToGenerate ->
        val candidatesForCurrentNumber = (0 until 0b1111111111).filter { canGenerateNumber(it, numberToGenerate) }

        val numberOfPreviousNumbers = if (candidates.isEmpty()) 0 else candidates.first().size
        when (numberOfPreviousNumbers) {
            0 -> candidatesForCurrentNumber.map { listOf(it) }
            1 -> candidates.flatMap { prev ->
                val (first) = prev
                candidatesForCurrentNumber.filter { second -> first shr 3 == second and 0b1111111 }
                    .map { second -> prev + second }
            }

            2 -> candidates.flatMap { prev ->
                val (first, second) = prev
                candidatesForCurrentNumber.filter { third -> second shr 3 == third and 0b1111111 && first shr 6 == third and 0b1111 }
                    .map { third -> prev + third }
            }

            else -> candidates.flatMap { prev ->
                val (first, second, third) = prev.takeLast(3)
                candidatesForCurrentNumber.filter { fourth -> third shr 3 == fourth and 0b1111111 && second shr 6 == fourth and 0b1111 && first shr 9 == fourth and 0b1 }
                    .map { fourth -> prev + fourth }
            }
        }.toSet()
    }

    val bestCandidate = candidates.minBy { it.last() }

    val result = bestCandidate.asReversed().fold(0L) { acc, n ->
        (acc shl 3) + (n and 0b111)
    }
    println(result)

    // check solution
    val output = execute(program, ComputerState(result, initialB, initialC, 0)).last().output
    println(output.joinToString(",") { it.toString() })
    println("Output correct: ${output == program}")
}

fun main() {
    part1(File("inputs/17-part1.txt"))
    part1(File("inputs/17.txt"))
    println("---")
    part2Example(File("inputs/17-part2.txt"))
    println("---")
    printProgram(File("inputs/17.txt"))
    part2(File("inputs/17.txt"))
}

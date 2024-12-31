package dev.ssch.day24

import dev.ssch.day05.split
import java.io.File
import kotlin.system.exitProcess

fun parseInitialValues(file: File): Map<String, Boolean> {
    return file.readLines().split { it.isEmpty() }[0].associate { line ->
        val (wire, value) = line.split(": ")
        Pair(wire, value.toInt() == 1)
    }
}

enum class Operator {
    And,
    Xor,
    Or
}

fun parseOperator(text: String): Operator {
    return when (text) {
        "AND" -> Operator.And
        "XOR" -> Operator.Xor
        "OR" -> Operator.Or
        else -> throw RuntimeException("Unknown operator: $text")
    }
}

data class Gate(
    val left: String,
    val right: String,
    val operator: Operator,
)

fun parseGates(file: File): Map<String, Gate> {
    val pattern = Regex("([a-z0-9]+) (AND|OR|XOR) ([a-z0-9]+) -> ([a-z0-9]+)")
    return file.readLines().split { it.isEmpty() }[1].associate { line ->
        val (left, operator, right, result) = pattern.matchEntire(line)!!.destructured
        Pair(result, Gate(left, right, parseOperator(operator)))
    }
}

fun part1(file: File) {
    val initialValues = parseInitialValues(file)
    val gates = parseGates(file)

    fun getValueOf(wire: String): Boolean {
        return initialValues[wire] ?: gates[wire]!!.let {
            val left = getValueOf(it.left)
            val right = getValueOf(it.right)
            when (it.operator) {
                Operator.And -> left && right
                Operator.Xor -> left xor right
                Operator.Or -> left || right
            }
        }
    }

    val result = gates.keys
        .asSequence()
        .filter { it.startsWith("z") }
        .sortedDescending()
        .map { getValueOf(it) }
        .map { if (it) 1 else 0 }
        .joinToString("")
        .toLong(2)
    println(result)
}

// ---

sealed class Query {
    abstract val resultName: String
    data class Operation(override val resultName: String, val operator: Operator, val left: Query, val right: Query) : Query()
    data class Wire(override val resultName: String, val wireName: String? = null) : Query()
}

fun Map<String, Gate>.findStructure(query: Query, start: String? = null, indent: Int = 0): List<Map<String, String>> {
    val gatesToConsider = filter { (wire, _) ->
        start == null || wire == start
    }

    return when (query) {
        is Query.Operation -> gatesToConsider.mapNotNull { (wire, gate) ->
            if (gate.operator == query.operator) {
                sequenceOf(
                    Pair(gate.left, gate.right),
                    Pair(gate.right, gate.left),
                ).map { (leftGate, rightGate) ->
                    Pair(
                        findStructure(query.left, leftGate, indent = indent + 1),
                        findStructure(query.right, rightGate, indent = indent + 1),
                    )
                }.flatMap { (leftResults, rightResults) ->
                    leftResults.flatMap { left ->
                        rightResults.map { right ->
                            left + right + (query.resultName to wire)
                        }
                    }
                }.toList()
            } else {
                null
            }
        }.flatten()

        is Query.Wire -> if (query.wireName == null) {
            gatesToConsider.keys.map {
                mapOf(query.resultName to it)
            }
        } else {
            if (start == query.wireName) {
                listOf(mapOf(query.resultName to query.wireName))
            } else {
                emptyList()
            }
        }
    }
}

fun part2(file: File) {
    val gates = parseGates(file).toMutableMap()

    fun toGraphViz(name: String) {
        File("outputs/${file.name}.graph-$name.txt").writer().use {
            it.appendLine("digraph {")
            gates.entries.forEach { (wire, gate) ->
                it.appendLine("  ${gate.left} -> $wire [label=\"${gate.operator}\"]")
                it.appendLine("  ${gate.right} -> $wire [label=\"${gate.operator}\"]")
            }
            it.appendLine("}")
        }
    }

    toGraphViz("initial")

    // check z00
    val z00Wires = gates.findStructure(
        Query.Operation(
            "xor",
            Operator.Xor,
            Query.Wire("x", "x00"),
            Query.Wire("y", "y00"),
        )
    ).firstOrNull()

    if (z00Wires == null) {
        println("unrecoverable error in circuit")
        exitProcess(0)
    } else if (z00Wires["xor"] == "z00") {
        println("z00 ok")
    }

    // check and identify c00
    val c00Wires = gates.findStructure(
        Query.Operation(
            "and",
            Operator.And,
            Query.Wire("x", "x00"),
            Query.Wire("y", "y00"),
        )
    ).firstOrNull()

    if (c00Wires == null) {
        println("unrecoverable error in circuit")
        exitProcess(0)
    } else {
        println("c00 ok")
    }

    var previousCarry = c00Wires["and"]

    val wrongWires = mutableSetOf<String>()

    fun swapWires(wireA: String, wireB: String) {
        val temp = gates[wireA]!!
        gates[wireA] = gates[wireB]!!
        gates[wireB] = temp
    }

    (1..44).forEach { n ->
        val (inX, inY, outZ) = sequenceOf("x", "y", "z").map { "%s%02d".format(it, n) }.toList()
        println("checking $outZ using carry $previousCarry")

        // zNN = (xNN xor yNN) xor c[NN-1]

        val fittingAssignmentForResultBit = gates.findStructure(
            Query.Operation(
                "xor2",
                Operator.Xor,
                Query.Operation("xor1", Operator.Xor, Query.Wire("x", inX), Query.Wire("y", inY)),
                Query.Wire("c", previousCarry)
            )
        ).firstOrNull()

        if (fittingAssignmentForResultBit == null) {
            println("  result bit not ok                   -> manual check")
        } else if (fittingAssignmentForResultBit["xor2"] != outZ) {
            val otherWire = fittingAssignmentForResultBit["xor2"]!!
            println("  simple swap in result bit -> fixed: $outZ and $otherWire")
            wrongWires += outZ
            wrongWires += otherWire
            swapWires(outZ, otherWire)
        }

        if (fittingAssignmentForResultBit != null && previousCarry != fittingAssignmentForResultBit["c"]) {
            println("  result bit uses carry: ${fittingAssignmentForResultBit["c"]}")
        }

        // cNN = (xNN and yNN) or ((xNN xor yNN) and c[NN-1])

        val fittingAssignmentForCarryBit = gates.findStructure(
            Query.Operation(
                "or",
                Operator.Or,
                Query.Operation(
                    "and1",
                    Operator.And,
                    Query.Wire("x1", inX),
                    Query.Wire("y1", inY)
                ),
                Query.Operation(
                    "and2",
                    Operator.And,
                    Query.Operation(
                        "xor",
                        Operator.Xor,
                        Query.Wire("x2", inX),
                        Query.Wire("y2", inY),
                    ),
                    Query.Wire("c", previousCarry)
                )
            )
        ).firstOrNull()

        if (fittingAssignmentForCarryBit != null) {
            if (previousCarry != fittingAssignmentForCarryBit["c"]) {
                println("  carry bit uses carry: ${fittingAssignmentForCarryBit["c"]}")
            }
            previousCarry = fittingAssignmentForCarryBit["or"]!!
        } else {
            println("  carry bit not ok                   -> manual check")
            previousCarry = null
        }
    }

    if (previousCarry == "z45") {
        println("z45 ok")
    }

    println("-----------")
    println(wrongWires.sorted().joinToString(","))
    toGraphViz("with-fixes")
    println("TODO: Find remaining errors in GraphViz")
}

fun main() {
    part1(File("inputs/24-part1-1.txt"))
    part1(File("inputs/24-part1-2.txt"))
    part1(File("inputs/24.txt"))
    println("---")
    part2(File("inputs/24.txt"))
}

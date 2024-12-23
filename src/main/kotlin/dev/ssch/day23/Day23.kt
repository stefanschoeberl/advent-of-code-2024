package dev.ssch.day23

import java.io.File

fun <T> Set<T>.generatePowerSetWith2Elements(): Set<Set<T>> {
    return flatMap { firstElement ->
        filter { it != firstElement }
            .map { secondElement -> setOf(firstElement, secondElement) }
    }.toSet()
}

private fun parseConnections(cables: List<Pair<String, String>>): Map<String, Set<String>> {
    return cables.asSequence()
        .flatMap { sequenceOf(it, Pair(it.second, it.first)) }
        .groupBy { it.first }
        .mapValues { (_, value) -> value.map { it.second }.toSet() }
}

private fun parseCables(file: File): List<Pair<String, String>> {
    return file.readLines().map {
        val (computer1, computer2) = it.split("-")
        Pair(computer1, computer2)
    }
}

fun part1(file: File) {
    val cables = parseCables(file)
    val connections = parseConnections(cables)

    val triples = connections.entries
        .filter { (key, _) -> key.startsWith("t") }
        .flatMap { (key, connectedComputers) ->
            connectedComputers.generatePowerSetWith2Elements()
                .map { it.toList() }
                .filter { (a, b) -> connections[a]!!.contains(b) }
                .map { (it + key).toSet() }
        }.toSet()

    println(triples.size)
}

// ---

fun part2(file: File) {
    val cables = parseCables(file)
    val connections = parseConnections(cables)

    fun extendNetworkByAddingOneNode(network: Set<String>): Set<Set<String>> {
        return network
            .mapNotNull { connections[it]?.filter { newNode -> newNode !in network }?.toSet() }
            .reduce { a, b -> a intersect b }
            .map { newNode ->
                network + newNode
            }.toSet()
    }

    val lanParty = generateSequence(connections.keys.map { setOf(it) }.toSet()) { currentNetworks ->
        currentNetworks.flatMap {
            extendNetworkByAddingOneNode(it)
        }.toSet().takeIf { it.isNotEmpty() }
    }.last().first()

    println(lanParty.sorted().joinToString(","))
}

fun main() {
    part1(File("inputs/23-part1.txt"))
    part1(File("inputs/23.txt"))
    println("---")
    part2(File("inputs/23-part1.txt"))
    part2(File("inputs/23.txt"))
}

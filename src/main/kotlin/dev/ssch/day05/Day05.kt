package dev.ssch.day05

import java.io.File

fun <T> List<T>.split(pred: (T) -> Boolean): List<List<T>> {
    data class Acc(
        val result: List<List<T>>,
        val currentChunk: List<T>,
    )

    return foldIndexed(Acc(emptyList(), emptyList())) { index: Int, acc: Acc, value: T ->
        if (pred(value)) {
            // splitter element found, add current chunk to result, discard current element
            Acc(acc.result.plusElement(acc.currentChunk), emptyList())
        } else if (index == size - 1) {
            // last (non-splitter) element encountered, finish last chunk and add it to the result
            Acc(acc.result.plusElement(acc.currentChunk + value), emptyList())
        } else {
            // add current (non-splitter) element to current chunk
            Acc(acc.result, acc.currentChunk + value)
        }
    }.result
}

fun isValidUpdate(update: List<Int>, ordering: List<Pair<Int, Int>>): Boolean {
    return ordering.all {
        val lastIndexOfSmallerValue = update.lastIndexOf(it.first)
        val firstIndexOfBiggerValue = update.indexOf(it.second)
        lastIndexOfSmallerValue == -1 || firstIndexOfBiggerValue == -1 || lastIndexOfSmallerValue < firstIndexOfBiggerValue
    }
}

fun part1(file: File) {
    val lines = file.readLines()
    val (orderingLines, updateLines) = lines.split { it.isEmpty() }

    val ordering = orderingLines
        .map { it.split("|") }
        .map { Pair(it[0].toInt(), it[1].toInt()) }

    val updates = updateLines.map { it.split(",").map { it.toInt() } }

    val result = updates
        .filter { isValidUpdate(it, ordering) }
        .sumOf { it[it.size / 2] }
    println(result)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()
    val (orderingLines, updateLines) = lines.split { it.isEmpty() }

    val ordering = orderingLines
        .map { it.split("|") }
        .map { Pair(it[0].toInt(), it[1].toInt()) }

    val updates = updateLines.map { it.split(",").map { it.toInt() } }

    val comparator = Comparator<Int> { v1, o2 ->
        ordering.find {
            (it.first == v1 && it.second == o2) || (it.first == o2 && it.second == v1)
        }?.let { if (it.first == v1) 1 else -1 } ?: 0
    }

    val result = updates
        .filter { !isValidUpdate(it, ordering) }
        .map {
            it.sortedWith(comparator)
        }.sumOf { it[it.size / 2] }

    println(result)
}

fun main() {
    part1(File("inputs/05-part1.txt"))
    part1(File("inputs/05.txt"))
    println("---")
    part2(File("inputs/05-part1.txt"))
    part2(File("inputs/05.txt"))
}

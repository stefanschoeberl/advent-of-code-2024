package dev.ssch.day09

import java.io.File

data class Block(
    val id: Long,
    val size: Long,
) {
    fun isEmpty(): Boolean = id == -1L

    fun shrinkBy(size: Long): Block {
        return Block(id, this.size - size)
    }

    fun resizeTo(size: Long): Block {
        return Block(id, size)
    }
}

fun emptyBlock(size: Long): Block {
    return Block(-1, size)
}

fun gaussSum(n: Long): Long {
    return (n * n + n) / 2
}

fun gaussSum(from: Long, to: Long): Long {
    return gaussSum(to) - gaussSum(from) + from
}

fun parseLine(line: String): List<Block> {
    return line.mapIndexed { index, ch ->
        if (index % 2 == 0) {
            Block(index.div(2L), ch.digitToInt().toLong())
        } else {
            emptyBlock(ch.digitToInt().toLong())
        }
    }
}

fun part1(file: File) {
    val line = file.readLines().first()

    val blocks = parseLine(line).toMutableList()

    fun findFirstEmptyBlockWithSpace(): Int {
        return blocks.indexOfFirst { it.isEmpty() && it.size > 0 }
    }

    fun findLastBlockToCompress(): Int {
        return blocks.indexOfLast { !it.isEmpty() && it.size > 0 }
    }

    var currentEmptyBlockIndex = findFirstEmptyBlockWithSpace()
    while (currentEmptyBlockIndex != -1) {
        val currentEmptyBlock = blocks[currentEmptyBlockIndex]
        val blockToMoveIndex = findLastBlockToCompress()
        val blockToMove = blocks[blockToMoveIndex]

        if (currentEmptyBlock.size >= blockToMove.size) {
            blocks.removeAt(blockToMoveIndex)
            blocks.removeAt(currentEmptyBlockIndex)

            blocks.add(currentEmptyBlockIndex, emptyBlock(currentEmptyBlock.size - blockToMove.size))
            blocks.add(currentEmptyBlockIndex, blockToMove)
        } else {
            blocks.removeAt(blockToMoveIndex)
            blocks.add(blockToMoveIndex, blockToMove.shrinkBy(currentEmptyBlock.size))

            blocks.removeAt(currentEmptyBlockIndex)
            blocks.add(currentEmptyBlockIndex, blockToMove.resizeTo(currentEmptyBlock.size))
        }
        currentEmptyBlockIndex = findFirstEmptyBlockWithSpace()
    }

    val offsets = blocks.runningFold(0L) { offset, block ->
        offset + block.size
    }

    val result = offsets.zip(blocks).sumOf { (offset, block) ->
        block.id * gaussSum(offset, offset + block.size - 1)
    }

    println(result)
}

fun printBlocks(blocks: List<Block>) {
    blocks.forEach { block ->
        if (block.isEmpty()) {
            print(".".repeat(block.size.toInt()))
        } else {
            print(block.id.toString().repeat(block.size.toInt()))
        }
    }
    println()
}

// ---

fun part2(file: File) {
    val line = file.readLines().first()

    val blocks = parseLine(line).toMutableList()

    fun findFirstEmptyBlockWithSpace(minimumSize: Long): Int {
        return blocks.indexOfFirst { it.isEmpty() && it.size >= minimumSize }
    }

    val maxBlockId = blocks.maxOf { it.id }
    for (blockId in maxBlockId downTo 1) {
        val blockToMoveIndex = blocks.indexOfFirst { it.id == blockId }
        val blockToMove = blocks[blockToMoveIndex]
        val emptyBlockIndex = findFirstEmptyBlockWithSpace(blockToMove.size)
        if (emptyBlockIndex != -1 && emptyBlockIndex < blockToMoveIndex) {
            val currentEmptyBlock = blocks[emptyBlockIndex]

            blocks.removeAt(blockToMoveIndex)
            blocks.add(blockToMoveIndex, emptyBlock(blockToMove.size))
            blocks.removeAt(emptyBlockIndex)

            blocks.add(emptyBlockIndex, emptyBlock(currentEmptyBlock.size - blockToMove.size))
            blocks.add(emptyBlockIndex, blockToMove)
        }
    }

    val offsets = blocks.runningFold(0L) { offset, block ->
        offset + block.size
    }

    val result = offsets.zip(blocks)
        .filter { !it.second.isEmpty() }
        .sumOf { (offset, block) ->
            block.id * gaussSum(offset, offset + block.size - 1)
        }
    println(result)
}

fun main() {
    part1(File("inputs/09-part1.txt"))
    part1(File("inputs/09.txt"))
    println("---")
    part2(File("inputs/09-part1.txt"))
    part2(File("inputs/09.txt"))
}

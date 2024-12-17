package dev.ssch.day16

import dev.ssch.day12.Position
import dev.ssch.day12.positionsWithLabel
import dev.ssch.day15.Direction
import dev.ssch.day15.move
import java.io.File

typealias Grid = List<String>

private fun parseStart(grid: Grid) =
    grid.positionsWithLabel.first { (_, ch) -> ch == 'S' }.let { (position, _) -> position }

private fun parseEnd(grid: Grid) =
    grid.positionsWithLabel.first { (_, ch) -> ch == 'E' }.let { (position, _) -> position }

private fun parseWalls(grid: Grid) =
    grid.positionsWithLabel.filter { (_, ch) -> ch == '#' }.map { (position, _) -> position }.toSet()

fun Direction.turnLeft(): Direction {
    return when (this) {
        Direction.Up -> Direction.Left
        Direction.Right -> Direction.Up
        Direction.Down -> Direction.Right
        Direction.Left -> Direction.Down
    }
}

fun Direction.turnRight(): Direction {
    return when (this) {
        Direction.Up -> Direction.Right
        Direction.Right -> Direction.Down
        Direction.Down -> Direction.Left
        Direction.Left -> Direction.Up
    }
}

data class Node (
    val position: Position,
    val direction: Direction,
)

fun part1(file: File) {
    val grid = file.readLines()

    val walls = parseWalls(grid)
    val start = parseStart(grid)
    val end = parseEnd(grid)

    val startNode = Node(start, Direction.Right)
    val openNodes = mutableSetOf(startNode)
    val closedNodes = mutableSetOf<Node>()
    val score = mutableMapOf(startNode to 0).withDefault { Int.MAX_VALUE }
    val prev = mutableMapOf<Node, Node?>(startNode to null)

    while (openNodes.isNotEmpty()) {
        val currentNode = openNodes.minByOrNull { score.getValue(it) }!!
        openNodes.remove(currentNode)
        if (currentNode !in closedNodes) {
            closedNodes.add(currentNode)

            val currentScore = score.getValue(currentNode)

            sequenceOf(
                Pair(Node(currentNode.position.move(currentNode.direction), currentNode.direction), 1),
                Pair(Node(currentNode.position, currentNode.direction.turnLeft()), 1000),
                Pair(Node(currentNode.position, currentNode.direction.turnRight()), 1000),
            ).filter { (node, stepCost) ->
                node.position !in walls && currentScore + stepCost < score.getValue(node)
            }.forEach { (node, stepCost) ->
                openNodes.add(node)
                score[node] = currentScore + stepCost
                prev[node] = currentNode
            }
        }

    }

    val (_, bestScore) = score.filter { it.key.position == end }.minBy { it.value }
    println(bestScore)
}

private fun printGrid(
    grid: Grid,
    walls: Set<Position>,
    path: Set<Position>,
) {
    grid.indices.forEach { row ->
        println(grid[0].indices.joinToString("") { column ->
            val pos = Position(row, column)
            if (pos in walls) {
                "#"
            } else if (pos in path) {
                "."
            } else {
                " "
            }
        })
    }
}

// ---

fun part2(file: File) {
    val grid = file.readLines()

    val walls = parseWalls(grid)
    val start = parseStart(grid)
    val end = parseEnd(grid)

    val startNode = Node(start, Direction.Right)
    val openNodes = mutableSetOf(startNode)
    val closedNodes = mutableSetOf<Node>()
    val score = mutableMapOf(startNode to 0).withDefault { Int.MAX_VALUE }
    val prev = mutableMapOf<Node, Set<Node>>().withDefault { emptySet() }

    while (openNodes.isNotEmpty()) {
        val currentNode = openNodes.minByOrNull { score.getValue(it) }!!
        openNodes.remove(currentNode)
        if (currentNode !in closedNodes) {
            closedNodes.add(currentNode)

            val currentScore = score.getValue(currentNode)

            sequenceOf(
                Pair(Node(currentNode.position.move(currentNode.direction), currentNode.direction), 1),
                Pair(Node(currentNode.position, currentNode.direction.turnLeft()), 1000),
                Pair(Node(currentNode.position, currentNode.direction.turnRight()), 1000),
            ).filter { (node, _) ->
                node.position !in walls
            }.forEach { (node, stepCost) ->
                if (currentScore + stepCost < score.getValue(node)) {
                    openNodes.add(node)
                    score[node] = currentScore + stepCost
                    prev[node] = setOf(currentNode)
                } else if (currentScore + stepCost == score.getValue(node)) {
                    prev[node] = prev.getValue(node) + currentNode
                }
            }
        }
    }


    val (endNode, _) = score.filter { it.key.position == end }.minBy { it.value }

    val visitedPositions = generateSequence(setOf(endNode)) { nodes ->
        nodes.flatMap { prev.getValue(it) }.toSet().takeIf { it.isNotEmpty() }
    }.flatten().map { it.position }.toSet()

    println(visitedPositions.size)
}

fun main() {
    part1(File("inputs/16-part1-1.txt"))
    part1(File("inputs/16-part1-2.txt"))
    part1(File("inputs/16.txt"))
    println("---")
    part2(File("inputs/16-part1-1.txt"))
    part2(File("inputs/16-part1-2.txt"))
    part2(File("inputs/16.txt"))
}

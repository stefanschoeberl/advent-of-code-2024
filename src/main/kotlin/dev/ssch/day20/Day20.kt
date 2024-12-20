package dev.ssch.day20

import dev.ssch.day12.Position
import dev.ssch.day12.inBounds
import dev.ssch.day12.positionsWithLabel
import dev.ssch.day15.Direction
import dev.ssch.day15.moveBy
import java.io.File

typealias Grid = List<String>

private fun parseStart(grid: Grid) =
    grid.positionsWithLabel.first { (_, ch) -> ch == 'S' }.let { (position, _) -> position }

private fun parseEnd(grid: Grid) =
    grid.positionsWithLabel.first { (_, ch) -> ch == 'E' }.let { (position, _) -> position }

private fun parseWalls(grid: Grid) =
    grid.positionsWithLabel.filter { (_, ch) -> ch == '#' }.map { (position, _) -> position }.toSet()

fun Position.neighboursAccessibleByCheating(maximumCheatDistance: Int): Set<Pair<Position, Int>> {
    return (0..maximumCheatDistance).asSequence().flatMap { horizontalDistance ->
        (0..(maximumCheatDistance - horizontalDistance)).asSequence().flatMap { verticalDistance ->
            sequenceOf(
                moveBy(horizontalDistance, Direction.Left).moveBy(verticalDistance, Direction.Up),
                moveBy(horizontalDistance, Direction.Left).moveBy(verticalDistance, Direction.Down),
                moveBy(horizontalDistance, Direction.Right).moveBy(verticalDistance, Direction.Up),
                moveBy(horizontalDistance, Direction.Right).moveBy(verticalDistance, Direction.Down),
            ).map {
                Pair(it, horizontalDistance + verticalDistance)
            }
        }
    }.toSet()
}

private fun calculateTimesToEnd(
    end: Position,
    walls: Set<Position>
): Map<Position, Int> {
    data class State(
        val position: Position,
        val previousPosition: Position?,
        val pathTime: Int,
    )
    return generateSequence(State(end, null, 0)) { (position, previousPosition, pathTime) ->
        position.neighbours().find {
            it != previousPosition && it !in walls
        }?.let { nextPosition ->
            State(nextPosition, position, pathTime + 1)
        }
    }.associateBy({ it.position }, { it.pathTime })
}

fun solve(file: File, minimumTimeSaved: Int, maximumCheatDistance: Int): Int {
    val grid = file.readLines()

    val start = parseStart(grid)
    val end = parseEnd(grid)
    val walls = parseWalls(grid)

    val timeToEnd = calculateTimesToEnd(end, walls)
    val timeWithoutCheats = timeToEnd[start]!!

    data class State(
        val position: Position,
        val previousPosition: Position?,
        val pathLength: Int,
        val cheatsFound: Int,
    )

    return generateSequence(State(start, null, 0, 0)) { (position, previousPosition, pathLength, cheatsFound) ->
        val numberOfCheats = position.neighboursAccessibleByCheating(maximumCheatDistance).count { (target, time) ->
            target !in walls && grid.inBounds(target) && pathLength + time + timeToEnd[target]!! <= timeWithoutCheats - minimumTimeSaved
        }

        position.neighbours().find {
            it != previousPosition && it !in walls
        }?.let { nextPosition ->
            State(nextPosition, position, pathLength + 1, cheatsFound + numberOfCheats)
        }
    }.last().cheatsFound
}

fun main() {
    fun testExample(minimumTimeSaved: Sequence<Int>, maximumCheatDistance: Int) {
        minimumTimeSaved.map {
            solve(File("inputs/20-part1.txt"), it, maximumCheatDistance)
        }.windowed(2, partialWindows = true).forEach {
            if (it.size == 1) {
                println(it[0])
            } else {
                println(it[0] - it[1])
            }
        }
    }

    testExample(sequenceOf(2, 4, 6, 8, 10, 12, 20, 36, 38, 40, 64), 2)
    println("---")
    println(solve(File("inputs/20.txt"), 100, 2))

    println("===")

    testExample(sequenceOf(50, 52, 54, 56, 58, 60, 62, 64, 66, 68, 70, 72, 74, 76), 20)
    println("---")
    println(solve(File("inputs/20.txt"), 100, 20))
}

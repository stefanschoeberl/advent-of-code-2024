package dev.ssch.day06

import java.io.File

enum class Direction {
    Up, Down, Left, Right;

    fun turn90DegreeRight(): Direction {
        return when (this) {
            Up -> Right
            Down -> Left
            Left -> Up
            Right -> Down
        }
    }
}

data class Position(val row: Int, val col: Int) {
    fun move(direction: Direction): Position {
        return when (direction) {
            Direction.Up -> Position(row - 1, col)
            Direction.Down -> Position(row + 1, col)
            Direction.Left -> Position(row, col - 1)
            Direction.Right -> Position(row, col + 1)
        }
    }
}

fun parseInput(lines: List<String>): Pair<Set<Position>, Position> {
    val obstacles = lines.flatMapIndexed { row: Int, line: String ->
        line.mapIndexedNotNull { col, ch ->
            if (ch == '#') Position(row, col) else null
        }
    }.toSet()

    val initialGuardPosition = lines.asSequence().mapIndexedNotNull { row, line ->
        val col = line.indexOf("^")
        if (col == -1) null else Position(row, col)
    }.first()
    return Pair(obstacles, initialGuardPosition)
}

fun calculateGuardPositions(
    initialGuardPosition: Position,
    obstacles: Set<Position>,
    rowIndices: IntRange,
    colIndices: IntRange
): Set<Position> {
    data class State(
        val guard: Position,
        val direction: Direction,
    )

    val visitedPositions = generateSequence(State(initialGuardPosition, Direction.Up)) {
        val nextGuardPosition = it.guard.move(it.direction)
        if (nextGuardPosition.row !in rowIndices || nextGuardPosition.col !in colIndices) {
            null
        } else if (nextGuardPosition in obstacles) {
            State(it.guard, it.direction.turn90DegreeRight())
        } else {
            State(nextGuardPosition, it.direction)
        }
    }.map { it.guard }.toSet()
    return visitedPositions
}

fun part1(file: File) {
    val lines = file.readLines()

    val (obstacles, initialGuardPosition) = parseInput(lines)
    val visitedPositions = calculateGuardPositions(initialGuardPosition, obstacles, lines.indices, lines[0].indices)

    println(visitedPositions.size)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()

    val (obstacles, initialGuardPosition) = parseInput(lines)

    val possibleObstaclePositions = calculateGuardPositions(
        initialGuardPosition,
        obstacles,
        lines.indices,
        lines[0].indices
    ) - initialGuardPosition

    fun createsLoop(addedObstacle: Position): Boolean {
        data class State(
            val guard: Position,
            val direction: Direction,
            val steps: Int
        )

        val newObstacles = obstacles + addedObstacle
        val maximumSteps = (lines.size * lines[0].length - newObstacles.size - 1) * 4
        return generateSequence(State(initialGuardPosition, Direction.Up,0)) {
            val nextGuardPosition = it.guard.move(it.direction)
            if (it.steps == maximumSteps || nextGuardPosition.row !in lines.indices || nextGuardPosition.col !in lines[0].indices) {
                null
            } else if (nextGuardPosition in newObstacles) {
                State(it.guard, it.direction.turn90DegreeRight(), it.steps + 1)
            } else {
                State(nextGuardPosition, it.direction, it.steps + 1)
            }
        }.last().steps == maximumSteps
    }

    val result = possibleObstaclePositions.count { createsLoop(it) }
    println(result)
}

fun main() {
    part1(File("inputs/06-part1.txt"))
    part1(File("inputs/06.txt"))
    println("---")
    part2(File("inputs/06-part1.txt"))
    part2(File("inputs/06.txt"))
}

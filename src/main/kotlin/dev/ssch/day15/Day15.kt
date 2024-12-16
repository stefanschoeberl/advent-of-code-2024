package dev.ssch.day15

import dev.ssch.day05.split
import dev.ssch.day12.inBounds
import dev.ssch.day12.Position
import dev.ssch.day12.positionsWithLabel
import java.io.File

enum class Direction {
    Up,
    Right,
    Down,
    Left,
}

fun parseDirection(ch: Char): Direction {
    return when (ch) {
        '^' -> Direction.Up
        '<' -> Direction.Left
        '>' -> Direction.Right
        'v' -> Direction.Down
        else -> throw RuntimeException("Unknown direction $ch")
    }
}

fun Position.moveBy(delta: Int, direction: Direction): Position {
    return when (direction) {
        Direction.Up -> Position(row - delta, column)
        Direction.Down -> Position(row + delta, column)
        Direction.Right -> Position(row, column + delta)
        Direction.Left -> Position(row, column - delta)
    }
}

fun Position.move(direction: Direction): Position {
    return moveBy(1, direction)
}

typealias Grid = List<String>

private fun parseRobot(grid: Grid) =
    grid.positionsWithLabel.first { (_, ch) -> ch == '@' }.let { (position, _) -> position }

private fun parseBoxes(grid: Grid) =
    grid.positionsWithLabel.filter { (_, ch) -> ch == 'O' }.map { (position, _) -> position }.toSet()

private fun parseWalls(grid: Grid) =
    grid.positionsWithLabel.filter { (_, ch) -> ch == '#' }.map { (position, _) -> position }.toSet()

fun part1(file: File) {
    val (grid, moveLines) = file.readLines().split { it.isEmpty() }

    val walls = parseWalls(grid)
    val boxes = parseBoxes(grid)
    val robotStart = parseRobot(grid)

    val moves = moveLines.flatMap { it.map { parseDirection(it) } }

    data class State(
        val robotPosition: Position,
        val boxes: Set<Position>,
    )

    val finalBoxes = moves.fold(State(robotStart, boxes)) { (robot, currentBoxes), direction ->
        val emptyPositionInDirection =
            generateSequence(robot) { it.move(direction).takeIf { grid.inBounds(it) && it !in walls } }
                .firstOrNull { it != robot && it !in currentBoxes }
        val targetPosition = robot.move(direction)
        if (emptyPositionInDirection == targetPosition) {
            // move to empty position
            State(targetPosition, currentBoxes)
        } else if (emptyPositionInDirection != null) {
            // box at target position moves ("teleports") to empty position
            State(targetPosition, currentBoxes - targetPosition + emptyPositionInDirection)
        } else {
            // discard move
            State(robot, currentBoxes)
        }
    }.boxes

    val result = finalBoxes.sumOf { it.row * 100 + it.column }
    println(result)
}

// ---

fun part2(file: File) {
    val (grid, moveLines) = file.readLines().split { it.isEmpty() }

    val walls = parseWalls(grid).flatMap {
        sequenceOf(
            Position(it.row, it.column * 2),
            Position(it.row, it.column * 2 + 1)
        )
    }.toSet()
    val boxes = parseBoxes(grid).map {
        Position(it.row, it.column * 2)
    }.toSet()
    val robotStart = parseRobot(grid).let { Position(it.row, it.column * 2) }

    val moves = moveLines.flatMap { it.map { parseDirection(it) } }

    data class State(
        val robotPosition: Position,
        val boxes: Set<Position>,
    )

    fun isBox(position: Position, boxes: Set<Position>): Boolean {
        return boxes.asSequence()
            .flatMap { sequenceOf(it, it.right()) }
            .any { it == position }
    }

    val finalBoxes = moves.fold(State(robotStart, boxes)) { (robot, currentBoxes), direction ->
        val targetPosition = robot.move(direction)
        if (targetPosition !in walls && !isBox(targetPosition, currentBoxes)) {
            // move robot
            State(targetPosition, currentBoxes)
        } else {
            val boxesToMove = findBoxesToMove(robot, direction, walls, currentBoxes)
            if (boxesToMove.isNotEmpty()) {
                // move boxes and robot
                val movedBoxes = boxesToMove.map { it.move(direction) }.toSet()
                State(targetPosition, currentBoxes - boxesToMove + movedBoxes)
            } else {
                // discard move
                State(robot, currentBoxes)
            }
        }
    }.boxes

    val result = finalBoxes.sumOf { it.row * 100 + it.column }
    println(result)
}

private fun findBoxesToMove(
    robot: Position,
    direction: Direction,
    walls: Set<Position>,
    currentBoxes: Set<Position>
): Set<Position> {
    val initialBoxesToMove = when (direction) {
        Direction.Up, Direction.Down -> sequenceOf(robot.move(direction), robot.left().move(direction))
        Direction.Right -> sequenceOf(robot.right())
        Direction.Left -> sequenceOf(robot.moveBy(2, Direction.Left))
    }.filter { it in currentBoxes }.toSet()

    data class State(
        val foundBoxes: Set<Position>,
        val done: Boolean,
    )
    val boxesToMove = generateSequence(State(initialBoxesToMove, false)) { (foundBoxes, _) ->
        if (foundBoxes.isEmpty() || foundBoxes.any {
                it.move(direction) in walls || it.right().move(direction) in walls
            }) {
            State(emptySet(), true)
        } else {
            val newBoxes = foundBoxes
                .flatMap {
                    when (direction) {
                        Direction.Up, Direction.Down -> sequenceOf(
                            it.move(direction),
                            it.left().move(direction),
                            it.right().move(direction)
                        )

                        Direction.Left, Direction.Right -> sequenceOf(it.move(direction).move(direction))
                    }
                }
                .filter { it in currentBoxes && it !in foundBoxes }
            State(foundBoxes + newBoxes, newBoxes.isEmpty())
        }
    }.first { it.done }
    return boxesToMove.foundBoxes
}

private fun printGrid(
    grid: Grid,
    walls: Set<Position>,
    boxes: Set<Position>,
    robot: Position?
) {
    grid.indices.forEach { row ->
        println((0 until grid[0].length * 2).joinToString("") { column ->
            val pos = Position(row, column)
            if (pos == robot) {
                "@"
            } else if (pos in walls) {
                "#"
            } else if (pos in boxes) {
                "["
            } else if (pos.left() in boxes) {
                "]"
            } else {
                "."
            }
        })
    }
}

fun main() {
    part1(File("inputs/15-part1-1.txt"))
    part1(File("inputs/15-part1-2.txt"))
    part1(File("inputs/15.txt"))
    println("---")
    part2(File("inputs/15-part2.txt"))
    part2(File("inputs/15-part1-2.txt"))
    part2(File("inputs/15.txt"))
}

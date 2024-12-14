package dev.ssch.day14

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

data class Vector(val x: Int, val y: Int)

data class Robot(
    val position: Vector,
    val velocity: Vector,
)

fun parseRobot(line: String): Robot {
    val regex = Regex("p=(-?[0-9]+),(-?[0-9]+) v=(-?[0-9]+),(-?[0-9]+)")
    val (px, py, vx, vy) = regex.matchEntire(line)!!.destructured
    return Robot(
        Vector(
            px.toInt(),
            py.toInt(),
        ),
        Vector(
            vx.toInt(),
            vy.toInt(),
        )
    )
}

fun endPositionAfterIterations(robot: Robot, n: Int, width: Int, height: Int): Vector {
    return Vector(
        (robot.position.x + n * (robot.velocity.x + width)) % width,
        (robot.position.y + n * (robot.velocity.y + height)) % height,
    )
}

fun part1(file: File, width: Int, height: Int) {
    val lines = file.readLines()

    val robots = lines.map { parseRobot(it) }
    val endPositions = robots.map { endPositionAfterIterations(it, 100, width, height) }

    val q1 = endPositions.count { it.x < width / 2 && it.y < height / 2 }
    val q2 = endPositions.count { it.x > width / 2 && it.y < height / 2 }
    val q3 = endPositions.count { it.x < width / 2 && it.y > height / 2 }
    val q4 = endPositions.count { it.x > width / 2 && it.y > height / 2 }

    println(q1 * q2 * q3 * q4)
}

// ---

fun part2(file: File, width: Int, height: Int) {
    val lines = file.readLines()
    val robots = lines.map { parseRobot(it) }

    File("outputs/14").mkdirs()
    (1..10000).forEach { iterations ->
        val endPositions = robots.map { endPositionAfterIterations(it, iterations, width, height) }.toSet()
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        (0 until image.width).forEach { x ->
            (0 until image.height).forEach { y ->
                image.setRGB(x, y, if (Vector(x, y) in endPositions) 0xffffff else 0x000000)
            }
        }
        ImageIO.write(image, "png", File("outputs/14/$iterations.png"))
    }
}

fun main() {
    part1(File("inputs/14-part1.txt"), 11, 7)
    part1(File("inputs/14.txt"), 101, 103)
    println("---")
    part2(File("inputs/14.txt"), 101, 103)
}

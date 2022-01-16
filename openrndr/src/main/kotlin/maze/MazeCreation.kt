package maze

import kotlinx.coroutines.delay
import org.openrndr.PresentationMode
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.extra.noclear.NoClear
import org.openrndr.launch


val BG_COLOR = ColorRGBa(.1, .1, .1)
val WALL_COLOR = ColorRGBa(.3, .0, .3)
val VISITED_COLOR = ColorRGBa(.8, .2, .3) //ColorRGBa(.0, .9, .7)
val ACTIVE_COLOR = ColorRGBa(.99, .5, .8) //ColorRGBa(0.0, 0.8, 0.3)



class Maze(width: Int, height: Int, val cellSize: Int = 40) {

    val cols = width / cellSize
    val rows = height / cellSize
    val grid = (0 until rows).map { y -> (0 until cols).map { x -> Cell(x, y) } }.flatten()

    fun index(i: Int, j: Int) = if (i in 0 until cols && j in 0 until rows) i + cols * j else -1

    fun addNeighbour(x: Int, y: Int, neighbourhood: MutableList<Cell>) {
        val index = index(x, y)
        if (index != -1 && !grid[index].visited) {
            neighbourhood.add(grid[index])
        }
    }

    fun unvisitedNeighbour(cell: Cell): Cell? {
        val neighbourhood = mutableListOf<Cell>()
        with(cell) {
            addNeighbour(x + 1, y, neighbourhood)
            addNeighbour(x - 1, y, neighbourhood)
            addNeighbour(x, y + 1, neighbourhood)
            addNeighbour(x, y - 1, neighbourhood)
        }
        return neighbourhood.randomOrNull()
    }

    fun breakWall(current: Cell, next: Cell) {
        val x = current.x - next.x
        val y = current.y - next.y
        if (x != 0) {
            current.walls[2 + x] = false
            next.walls[2 - x] = false
        } else {
            current.walls[1 - y] = false
            next.walls[1 + y] = false
        }
    }


    class Explorer(start: Cell) {

        var current: Cell = start
        val stack = mutableListOf(current)


        fun explore(maze: Maze): Boolean {
            if (stack.isNotEmpty()) {
                current.visited = true
                current = when (val next = maze.unvisitedNeighbour(current)) {
                    null -> {
                        pop(stack)
                    }
                    else -> {
                        maze.breakWall(current, next)
                        push(current, stack)
                        next
                    }
                }
                return false
            }
            return true
        }

        fun <T> pop(stack: MutableList<T>): T {
            val element = stack.last()
            stack.removeLast()
            return element
        }

        fun <T> push(element: T, stack: MutableList<T>) {
            stack.add(element)
        }

    }

    class Cell(val x: Int, val y: Int) {
        val walls = arrayOf(true, true, true, true)
        var visited = false
    }

}


fun drawCells(maze: Maze, drawer: Drawer) {
    with(maze) {

        for (i in 0 until cols) {
            for (j in 0 until rows) {
                val index = maze.index(i, j)
                val cell = maze.grid[index]

                val x = i * cellSize.toDouble()
                val y = j * cellSize.toDouble()

                drawer.stroke = null
                drawer.fill = if (cell.visited) VISITED_COLOR else BG_COLOR
                drawer.rectangle(x, y, cellSize.toDouble(), cellSize.toDouble())

                drawer.fill = null
                drawer.stroke = WALL_COLOR
                drawer.strokeWeight = 1.0

                if (cell.walls[0]) drawer.lineSegment(x, y, x + cellSize, y)
                if (cell.walls[1]) drawer.lineSegment(x + cellSize, y, x + cellSize, y + cellSize)
                if (cell.walls[2]) drawer.lineSegment(x + cellSize, y + cellSize, x, y + cellSize)
                if (cell.walls[3]) drawer.lineSegment(x, y + cellSize, x, y)
            }
        }

    }
}

fun drawExplorer(explorer: Maze.Explorer, size: Double, drawer: Drawer) {
    val x = explorer.current.x * size
    val y = explorer.current.y * size

    drawer.stroke = null
    drawer.fill = ACTIVE_COLOR
    drawer.rectangle(x, y, size, size)
}

fun drawCell(explorer: Maze.Explorer, maze: Maze, xOff: Int, yOff: Int, drawer: Drawer) {
    with(maze) {
        val index = index(explorer.current.x + xOff, explorer.current.y + yOff)
        if (index == -1) return

        val cell = maze.grid[index]
        val x = cell.x * cellSize.toDouble()
        val y = cell.y * cellSize.toDouble()

        drawer.stroke = null
        drawer.fill = if (cell.visited) VISITED_COLOR else BG_COLOR
        drawer.rectangle(x, y, cellSize.toDouble(), cellSize.toDouble())

        drawer.fill = null
        drawer.stroke = WALL_COLOR
        drawer.strokeWeight = 1.0

        if (cell.walls[0]) drawer.lineSegment(x, y, x + cellSize, y)
        if (cell.walls[1]) drawer.lineSegment(x + cellSize, y, x + cellSize, y + cellSize)
        if (cell.walls[2]) drawer.lineSegment(x + cellSize, y + cellSize, x, y + cellSize)
        if (cell.walls[3]) drawer.lineSegment(x, y + cellSize, x, y)

    }
}

fun drawSurroundingCells(dora: Maze.Explorer, maze: Maze, drawer: Drawer) {
    drawCell(dora, maze, 1, 0, drawer)
    drawCell(dora, maze, -1, 0, drawer)
    drawCell(dora, maze, 0, 1, drawer)
    drawCell(dora, maze, 0, -1, drawer)
}


fun mazeApp(w: Int, h: Int, size: Int, frameRate: Int) = application {

    configure {
        width = w
        height = h
    }

    program {

        val maze = Maze(w, h, size)
        val index = maze.index(maze.cols / 2, maze.rows / 2)
        val start = maze.grid[index]

        val dora = Maze.Explorer(start)
        val marco = Maze.Explorer(start)
        val heyerdahl = Maze.Explorer(start)
        val armstrong = Maze.Explorer(start)
        val explorers = listOf(dora, marco, heyerdahl, armstrong)

        // fast render
        // optimized by not clearing the screen
        // and only redrawing the cells around the current cell

        extend(NoClear())
        var once = true
        extend {
            if (once) {
                drawCells(maze, drawer)
                once = false
            }

            for (e in explorers.shuffled()) {
                e.explore(maze)

                drawSurroundingCells(e, maze, drawer)
                drawExplorer(e, maze.cellSize.toDouble(), drawer)
            }


        }
    }
}


fun Program.frameRate(rate: Int) {
    this.application.presentationMode = PresentationMode.MANUAL
    launch {
        while (true) {
            delay(1000L / rate)
            window.requestDraw()
        }
    }
}


fun main() = mazeApp(900, 600, 10, 32)

package minesweeper

import java.util.*
import kotlin.random.Random


fun main() {
    val scanner = Scanner(System.`in`)
    print("How many mines do you want on the field? ")
    val bombQuantity : Int = scanner.nextInt()
    val game = Game(bombQuantity)
    while (!Game.solved) {
        game.prettyPrint()
        game.handleInput(scanner.getInput())
    }
}

class Cell (var mined: Boolean, var state: State, var neighbors: Int)

data class Input(var coordinate: Int, var action: Actions)

fun Scanner.getInput(): Input {
    println("Set/unset mine marks or claim a cell as free: ")
    val x: Int = nextInt()
    val y: Int = nextInt()
    val action: Actions = Actions.valueOf(next().toUpperCase())
    return Input((y-1) * 9 + (x-1), action)
}

enum class Actions { MINE, FREE }

enum class State(val char: Char){
    MARKED('*'),
    UNEXPLORED('.'),
    EXPLORED('/'),
    MINED('X')
}

class Game(val bombQuantity: Int){

    fun handleInput(input: Input){
        fun mark(coordinate: Int) {
            cellArray[coordinate].state = when (cellArray[coordinate].state) {
                State.MARKED -> State.UNEXPLORED
                State.UNEXPLORED -> State.MARKED
                else -> cellArray[coordinate].state
            }
            isSolved()
        }

        fun explore(coordinate: Int) {
            if (cellArray.filter { it -> it.mined }.isEmpty()) {
                placeBombs(bombQuantity, listOf(coordinate))
                assignNeighbourInts()
            }
            var cell = cellArray[coordinate]
            if ( cell.mined ) {
                gameOver()
            } else {
                cell.state = State.EXPLORED
                if (cell.neighbors == 0) {
                    neighbours(coordinate)
                        .filter { it -> it.state == State.UNEXPLORED || it.state == State.MARKED }
                        .forEach { it -> explore(cellArray.indexOf(it)) }
                }
            }
        }

        when ( input.action ) {
                Actions.MINE -> mark(input.coordinate)
                Actions.FREE -> explore(input.coordinate)
        }
    }

    companion object Board {
        var cellArray: Array<Cell> = Array(9 * 9) {  Cell(false, State.UNEXPLORED, -1) }
        var solved = false

        fun getSafeIndices(): List<Int> {
            return this.cellArray.withIndex()
                    .filter { !it.value.mined  }
                    .map { it.index }
        }

        fun placeBombs(quantity: Int, exclusion: List<Int?>): Unit {
            var index: Int
            var safeIndices: MutableList<Int>
            repeat(quantity) {
                safeIndices = getSafeIndices().toMutableList()
                safeIndices.removeAll(exclusion)
                index = when (safeIndices.size) {
                    0 -> getSafeIndices().first()
                    1 -> safeIndices.first()
                    else -> safeIndices[Random.nextInt(0, safeIndices.lastIndex)]
                }
                this.cellArray[index].mined = true
                this.cellArray[index].neighbors = -1
            }
        }

        fun neighbours(cell: Int): List<Cell> {
            val upperLeftX = cell % 9 + if (cell % 9 == 0) 0 else -1
            val upperLeftY = cell / 9 + if (cell / 9 == 0) 0 else -1
            val lowerRightX = cell % 9 + if (cell % 9 == 8) 0 else 1
            val lowerRightY = cell / 9 + if (cell / 9 == 8) 0 else 1
            var list = ArrayList<Cell>()
            for (i in upperLeftY..lowerRightY) {
                for (j in upperLeftX..lowerRightX) {
                    var index = 9 * i + j
                    if (cell != index) {
                        list.add(this.cellArray[index])
                    }
                }
            }
            return list.toList()
        }

        fun highlightMinedCells() {
            this.cellArray.filter { it.mined }.forEach { it.state = State.MINED }
        }

        fun numberOfNeighbourBombs(cell: Int): Int {
            return this.neighbours(cell).filter { it.mined }.size
        }

        fun assignNeighbourInts(): Unit {
            this.cellArray.forEach { it -> it.neighbors = numberOfNeighbourBombs(cellArray.indexOf(it)) }
        }

    }

    fun isSolved() {
        if ( cellArray.filter { it -> it.mined }.equals(cellArray.filter { it -> it.state == State.MARKED }) ) {
            solved = true
            println("Congratulations! You found all the mines!")
        }
    }

    fun gameOver() {
        highlightMinedCells()
        solved = true
        print("You stepped on a mine and failed!\n")
        prettyPrint()
    }

    fun prettyPrint() {
        val n = 9
        var output = " │123456789│\n"
        output += "—│—————————│\n"
        for (i in 0 until n) {
            output += "${i + 1}|" + cellArray
                    .sliceArray(n*i until (n*(i+1)))
                    .map { if (it.state == State.EXPLORED && it.neighbors != 0)
                        it.neighbors.toString().first() else it.state.char }
                    .joinToString("") + "|\n"
        }
        println("$output—│—————————│\n")
    }
}

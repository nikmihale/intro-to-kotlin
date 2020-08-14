package processor

import java.lang.Exception
import java.util.*
import kotlin.math.pow

fun main() {
    val scanner = Scanner(System.`in`)
    loop@while (true) {
        printMenu()
        var input = scanner.nextLine().toInt()
        when (input) {
            1 -> { println("The addition result\n" +
                (promptMatrix(scanner, "first") +  promptMatrix(scanner, "second"))) }
            2 -> { println("The multiplication result is:\n" +
                (promptMatrix(scanner, "first") * scanner.nextLine().trim().toDouble())) }
            3 -> { println("The multiplication result is:\n" +
                (promptMatrix(scanner, "first") *  promptMatrix(scanner, "second"))) }
            4 -> { transposeMenu()
                input = scanner.nextLine().toInt()
                when (input) {
                    1 -> { println("The result is:\n" +
                promptMatrix(scanner, "").transposeMain()) }
                    2 -> { println("The result is:\n" +
                promptMatrix(scanner, "").transposeSide()) }
                    3 -> { println("The result is:\n" +
                promptMatrix(scanner, "").transposeVertical()) }
                    4 -> { println("The result is:\n" +
                promptMatrix(scanner, "").transposeHorizontal()) }
                    }
                }
            5 -> { println("The result is:\n" +
                        promptMatrix(scanner, "").det().toString()) }
            6 -> { println("The result is:\n" +
                        promptMatrix(scanner, "").inverse()) }
            0 -> break@loop
            else -> {}
        }
    }
}

fun promptMatrix(scanner: Scanner, name: String): Matrix {
    println("Enter size of $name matrix: ")
    val (rows, columns) = scanner.nextLine().trim().split(' ').map { it.toInt() }
    val matrix = Matrix(rows, columns)
    println("Enter $name matrix: ")
    matrix.inputValues(scanner)
    return matrix
}

fun printMenu() = println("\n1. Add matrices\n" +
                        "2. Multiply matrix to a constant\n" +
                        "3. Multiply matrices\n" +
                        "4. Transpose matrix\n" +
                        "5. Calculate a determinant\n" +
                        "6. Inverse matrix\n" +
                        "0. Exit\n" +
                        "Your choice: ")

fun transposeMenu() = println("1. Main diagonal\n" +
                        "2. Side diagonal\n" +
                        "3. Vertical line\n" +
                        "4. Horizontal line\n" +
                        "Your choice: ")

class Matrix(val rows: Int, val columns: Int) {
    private var values = Array(rows) { Array(columns) { 0.0 } }

    fun inputValues(scanner: Scanner) {
        for (i in 0 until rows) {
            val line = scanner.nextLine()
                    .trim()
                    .split(' ')
                    .map { it.toDouble() }
                    .toTypedArray()
            this.values[i] = line
        }
    }

    fun inverse(): Matrix {
        val cofactorMatrix = Matrix(rows, columns)
        for (i in 0 until rows)
            for (j in 0 until columns)
                cofactorMatrix.values[i][j] = (-1.0).pow(i + j) * this.cofactor(i, j).det()
        return cofactorMatrix.transposeMain() * this.det().pow(-1)
    }

    private fun cofactor(i: Int, j: Int): Matrix {
        val minor = Matrix(rows - 1, columns - 1)
        minor.values = this.values
                .map { it.filterIndexed { index, _ -> index != j }.toTypedArray() }
                .filterIndexed { index, _ -> index != i}.toTypedArray()
        return minor
    }

    fun det(): Double {
        if (rows == 1) return values[0][0]
        var result = 0.0
        for (i in 0 until rows) {
            val elem = values[0][i]
            result += (-1.0).pow(i) * elem * cofactor(0, i).det()
        }
        return result
    }

    fun transposeMain(): Matrix {
        val result = Matrix(columns, rows)
        for (i in 0 until rows)
            for (j in 0 until columns)
                result.values[j][i] = this.values[i][j]
        return result
    }

    fun transposeSide(): Matrix {
        return this.transposeMain().transposeVertical().transposeHorizontal()
    }

    fun transposeHorizontal(): Matrix {
        val result = Matrix(rows, columns)
        result.values = this.values.reversedArray()
        return result
    }

    fun transposeVertical(): Matrix {
        val result = Matrix(rows, columns)
        result.values = this.values.map { it.reversedArray() }.toTypedArray()
        return result
    }

    operator fun plus(other: Matrix): Matrix {
        if (rows != other.rows || columns != other.columns) throw Exception("ERROR")
        val result = Matrix(rows, columns)
        result.values =
                this.values.flatten()
                        .zip(other.values.flatten()) { a, b -> a + b }
                        .chunked(columns) { it.toTypedArray() }
                        .toTypedArray()
        return result
    }

    operator fun times(coef: Double): Matrix {
        val result = Matrix(rows, columns)
        result.values = this.values.flatten()
                        .map { it * coef }
                        .chunked(columns) { it.toTypedArray() }
                        .toTypedArray()
        return result
    }

    operator fun times(other: Matrix): Matrix {
        val result = Matrix(rows, other.columns)
        for (i in 0 until rows)
            for (j in 0 until other.columns)
                for (k in 0 until columns)
                    result.values[i][j] += this.values[i][k] * other.values[k][j]
        return result
    }

    override fun toString(): String {
        return this.values.joinToString("\n") {
            it.joinToString(" ") }
    }
}
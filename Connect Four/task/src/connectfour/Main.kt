package connectfour

import org.w3c.dom.ranges.Range
import java.util.*

fun main() {

    val scanner = Scanner(System.`in`)
    println("Connect Four")
    val firstPlayersName = getPlayerName(scanner, true)
    val secondPlayersName = getPlayerName(scanner, false)
    val (rows, columns) = getBoardSize(scanner)
    val gameCount = getGameCount(scanner)
    var playerOnePoints = 0
    var playerTwoPoints = 0
    var playerOneStart = true

    println("$firstPlayersName VS $secondPlayersName")
    println("$rows X $columns board")
    println(if (gameCount == 1) "Single game" else "Total $gameCount games")

    loop@ for (game in 1..gameCount) {
        val gameGrid = mutableListOf<MutableList<String>>()
        for (i in 0 until rows) {
            gameGrid.add(i, mutableListOf())
            for (j in 0 until columns) {
                gameGrid[i].add(j, " ")
            }
        }

        if (gameCount > 1) {
            println("Game #$game")
        }

        printBoard(rows, columns, gameGrid)
        val results = playGame(scanner, firstPlayersName, secondPlayersName, gameGrid, columns, playerOneStart)

        when (results) {
            "end" -> {
                break@loop
            }
            "draw" -> {
                playerOnePoints += 1
                playerTwoPoints += 1
            }
            firstPlayersName -> {
                playerOnePoints += 2
            }
            secondPlayersName -> {
                playerTwoPoints += 2
            }
        }

        if (gameCount != 1) {
            println("Score\n$firstPlayersName: $playerOnePoints $secondPlayersName: $playerTwoPoints")
        }
        playerOneStart = !playerOneStart
    }

    println("Game over!")

}

fun getPlayerName(scanner: Scanner, firstPlayer: Boolean): String {
    val positionText = if (firstPlayer) "First" else "Second"
    println("$positionText player's name:")
    return scanner.nextLine()
}

fun getGameCount(scanner: Scanner): Int {
    println("Do you want to play single or multiple games?\nFor a single game, input 1 or press Enter\nInput a number of games:")
    val input = scanner.nextLine()
    var numGames = 0
    if (input.matches("[\\d+]+".toRegex())) {
        numGames = input.toInt()
        if (numGames <= 0) {
            println("Invalid input")
            numGames = getGameCount(scanner)
        }
    } else if (input.isEmpty()) {
        numGames = 1
    } else {
        println("Invalid input")
        numGames = getGameCount(scanner)
    }
    return numGames
}

fun getBoardSize(scanner: Scanner): Pair<Int, Int> {
    var pair = Pair(0, 0)
    val validateReg = Regex("[\\s+]*[\\d+]+[\\s+]*[xX][\\s+]*[\\d+]+[\\s+]*")
    var continueAsking = true
    while (continueAsking) {
        println("Set the board dimensions (Rows x Columns)")
        println("Press Enter for default (6 x 7)")
        val boardDimensions = scanner.nextLine()
        if (boardDimensions.isNotEmpty()) {
            if (boardDimensions.matches(validateReg)) {
                val temp = boardDimensions.toCharArray()
                    .filter { s -> s.isDigit() }
                    .map { it.digitToInt() }
                    .toMutableList()

                if (temp[0] in 5..9) {
                    if (temp[1] in 5..9) {
                        pair = Pair(temp[0], temp[1])
                        continueAsking = false
                    } else {
                        println("Board columns should be from 5 to 9")
                    }
                } else {
                    println("Board rows should be from 5 to 9")
                }
            } else {
                println("Invalid input")
            }
        } else {
            pair = Pair(6, 7)
            continueAsking = false
        }
    }

    return pair
}

fun printBoard(rows: Int, columns: Int, grid: MutableList<MutableList<String>>) {
    for (i in 0..rows + 1) {
        for (j in 0..columns) {
            if (i == 0) {
                if (j < columns) print(" ${j + 1}")
            } else if (i in 1..rows) {
                print("\u2551")
                if (j < columns) print(grid[i - 1][j])
            } else {
                when (j) {
                    0 -> {
                        print("\u255A")
                    }
                    columns -> {
                        print("\u2550\u255D")
                    }
                    else -> {
                        print("\u2550\u2569")
                    }
                }
            }

            if (j == columns) {
                print("\n")
            }
        }
    }
}

fun playGame(scanner: Scanner,
             playerOne: String,
             playerTwo: String,
             grid: MutableList<MutableList<String>>,
             columns: Int,
             playerOneStart: Boolean): String {
    val validateInput = "[\\d+]+".toRegex()
    var playerOneTurn = playerOneStart
    var input = "primer"
    while (input != "end") {
        if (playerOneTurn) {
            println("$playerOne's turn:")
        } else {
            println("$playerTwo's turn:")
        }

        input = scanner.nextLine()
        if (input.matches(validateInput)){
            val tmp = input.toInt()
            if (tmp in 1..columns) {
                if (!columnIsFull(grid, tmp-1)) {
                    val sym = if (playerOneTurn) "o" else "*"
                    addSymToGrid(grid, tmp-1, sym)
                    printBoard(grid.size, columns, grid)
                    if (playerWon(grid, sym)) {
                        if (playerOneTurn) {
                            println("Player $playerOne won")
                            return playerOne
                        } else {
                            println("Player $playerTwo won")
                            return playerTwo
                        }

                    } else if (draw(grid)) {
                        println("It is a draw")
                        return "draw"
                    }
                    playerOneTurn = !playerOneTurn
                } else {
                    println("Column $tmp is full")
                }
            } else {
                println("The column number is out of range (1 - $columns)")
            }
        } else {
            if (input != "end") {
                println("Incorrect column number")
            }
        }
    }

    return "end"
}

fun columnIsFull(grid: MutableList<MutableList<String>>, column: Int): Boolean {
    var full = true
    for (i in 0 until grid.size) {
        if (grid[i][column] == " ") {
            full = false
        }
    }
    return full
}

fun addSymToGrid(grid: MutableList<MutableList<String>>, column: Int, sym: String) {
    searchLoop@ for (i in grid.lastIndex downTo 0) {
        if (grid[i][column] == " ") {
            grid[i][column] = sym
            break@searchLoop
        }
    }
}

fun playerWon(grid: MutableList<MutableList<String>>, playerSym: String): Boolean {
    for (i in grid.indices) {
        for (j in grid[i].indices) {
            if (i <= grid.lastIndex - 3) {
                if (grid[i][j] == playerSym &&
                grid[i][j] == grid[i+1][j] &&
                grid[i+1][j] == grid[i+2][j] &&
                grid[i+2][j] == grid[i+3][j]) {
                    return true
                }

                if (j in 3..grid[i].lastIndex) {
                    if (grid[i][j] == playerSym &&
                        grid[i][j] == grid[i+1][j-1] &&
                        grid[i+1][j-1] == grid[i+2][j-2] &&
                        grid[i+2][j-2] == grid[i+3][j-3]) {
                        return true
                    }
                }
            }

            if (j <= grid[i].lastIndex - 3) {
                if (grid[i][j] == playerSym &&
                    grid[i][j] == grid[i][j+1] &&
                    grid[i][j+1] == grid[i][j+2] &&
                    grid[i][j+2] == grid[i][j+3]) {
                    return true
                }
            }

            if (j <= grid[i].lastIndex - 3 && i <= grid.lastIndex - 3) {
                if (grid[i][j] == playerSym &&
                    grid[i][j] == grid[i+1][j+1] &&
                    grid[i+1][j+1] == grid[i+2][j+2] &&
                    grid[i+2][j+2] == grid[i+3][j+3]) {
                    return true
                }
            }
        }
    }

    return false
}

fun draw(grid: MutableList<MutableList<String>>): Boolean {
    for (i in grid.indices) {
        for (j in grid.indices) {
            if (grid[i][j] == " ") {
                return false
            }
        }
    }

    return true
}
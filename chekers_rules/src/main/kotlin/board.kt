data class BoardChecker(val color: Int, var type: Int)

data class BoardField(val x: Int, val y: Int, val color: Int, var checker: BoardChecker? = null)

class CheckersBoard {
    private val board: List<BoardField>

    init {
        board = (1..8).flatMap { y -> (1..8).map { x -> BoardField(x, y, (x + y + 1) % 2 ) } }
    }

    private fun get(x: Int, y: Int) = board.find { it.x == x && it.y == y }

    fun print() {
        (8 downTo 1).forEach { y ->
            print("$y| ")
            (1..8).forEach { x ->
                print("   ".takeIf { get(x, y)?.color == 0 } ?: "[ ]")
            }
            println()
        }
        println("    a  b  c  d  e  f  g  h")
    }

    fun place(x: Int, y: Int, checker: BoardChecker?) {
        get(x, y)!!.checker = checker
    }
}

fun main(args: Array<String>) {
    CheckersBoard().print()
}

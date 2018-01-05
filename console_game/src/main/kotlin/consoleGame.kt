import java.util.*

fun main(args: Array<String>) {
    val game = GameController()
    val scanner = Scanner(System.`in`)
    var moves: List<String>
    val humanColor = 0
    val nw = NetworkIO().load("best.net")!!
    val player = Player(nw)
    while (true) {
        game.print()
        if (game.currentColor == 0) {
            println("Ход белых:")
        } else println("Ход чёрных:")
        moves = game.nextMoves()
        moves.forEachIndexed { index, move ->
            println("${index+1}) $move")
        }
        if (moves.isEmpty()) {
            if (game.currentColor == 0) println("чёрные победили")
            else println("белые победили")
            return
        }
        if (humanColor == game.currentColor) {
            while (true) {
                try {
                    print("Введите номер хода: ")
                    val step = scanner.nextInt()
                    game.go(moves[step - 1])
                    break
                } catch (e: Exception) {
                    println("Некорректный ввод")
                }
            }
        } else {
            println("board clone")
            val board = game.checkerboard
            val step = player.selectMove(game.checkerboard, game.currentColor, moves)
            GameController(board).print()
            game.go(step)
        }
        game.currentColor = 1 - game.currentColor
    }
}
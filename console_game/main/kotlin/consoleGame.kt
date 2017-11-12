import java.util.*

fun main(args: Array<String>) {
    val game = Game()
    val scanner = Scanner(System.`in`)
    var moves: List<String>
    val humanColor = 0
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
            val step = Random().nextInt(moves.size) + 1
            game.go(moves[step - 1])
        }
        game.currentColor = 1 - game.currentColor
    }
}
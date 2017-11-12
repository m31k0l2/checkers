fun main(args: Array<String>) {
    val game = Game()
    val moves: List<String>
    while (true) {
        game.print()
        if (game.currentColor == 0) {
            println("Ход белых:")
            moves = game.nextMoves()
            moves.forEachIndexed { index, move ->
                println("${index+1}) $move")
            }
            break
        }
    }
}
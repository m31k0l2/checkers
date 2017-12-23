val game = GameController()
val stepLimit = 50

fun step(player: Player, moves: List<String>) {
    val step = player.selectMove(game.checkerboard, game.currentColor, moves)
    game.go(step)
    game.currentColor = 1 - game.currentColor
}

fun play(net1: String, net2: String) {
    var moves: List<String>
    val nw1 = NetworkIO().load("$net1.net")!!
    val nw2 = NetworkIO().load("$net2.net")!!
    val white = Player(nw1, debug = true)
    val black = Player(nw2, debug = true)
    var curStep = 0
    while (curStep++ < stepLimit) {
        game.print()
        moves = game.nextMoves()
        if (moves.isEmpty()) {
            if (game.currentColor == 0) println("$net2 победила")
            else println("$net1 победила")
            return
        }
        if (curStep == stepLimit) {
            if (game.checkerboard.encodeToVector().sum() > 0) {
                println("$net1 победила")
            } else {
                println("$net2 победила")
            }
        }
        step(white.takeIf { game.currentColor == 0 } ?: black, moves)
    }
}

fun main(args: Array<String>) {
    play("40-10", "40-20")
}
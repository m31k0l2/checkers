import java.io.File

class EvolutionCheckers(populationSize: Int, scale: Int): AbstractEvolution(populationSize, scale) {
    override fun play(a: Network, b: Network): Int {
        val player1 = Player(a, 2)
        val player2 = Player(b, 2)
        val res = play(player1, player2)
        return 1.takeIf { res == 0 } ?: 0.takeIf { res == -1 } ?: -2
    }

    override fun createNet() = Network(91, 40, 10, 1)

    override fun generatePopulation(size: Int): List<Individual> {
        if (!File("save0.net").exists()) return super.generatePopulation(size)
        return (0 until size).map { Individual(NetworkIO().load("save$it.net")!!) }
    }

    private fun play(player1: Player, player2: Player): Int {
        val game = Game()
        var moves: List<String>
        val maxSteps = 50
        var curStep = 0
        while (++curStep < maxSteps) {
            moves = game.nextMoves()
            if (moves.isEmpty()) {
                return game.currentColor
            }
            val player = player1.takeIf { game.currentColor == 0 } ?: player2
            game.go(player.selectMove(game.checkerboard, game.currentColor, moves))
            game.currentColor = 1 - game.currentColor
        }
        return -1 // ничья
    }
}

fun main(args: Array<String>) {
    with(EvolutionCheckers(30, 5)) {
        val nw = evolute(2).nw
        NetworkIO().save(nw, "best.net")
    }
}
import java.io.File

class EvolutionCheckers(populationSize: Int, scale: Int, private val maxSteps: Int = 50): AbstractEvolution(populationSize, scale) {
    private var curEpoch = 0
    private val savePerEpoch = 5
    private lateinit var population: List<Individual>

    override fun play(a: Network, b: Network): Int {
        val player1 = Player(a, 2)
        val player2 = Player(b, 2)
        val res = play(player1, player2)
        return 1.takeIf { res == 0 } ?: -2
    }

    override fun createNet() = Network(91, 40, 10, 1)

    override fun generatePopulation(size: Int): List<Individual> {
        if (!File("save0.net").exists()) return super.generatePopulation(size)
        val io = NetworkIO()
        return (0 until size).map { Individual(io.load("save$it.net")!!) }
    }

    private fun play(player1: Player, player2: Player): Int {
        val game = Game()
        var moves: List<String>
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
        val white = game.checkerboard.encodeToVector().filter { it > 0 }.sum()
        val black = game.checkerboard.encodeToVector().filter { it < 0 }.sum()
        return if (white > Math.abs(black)) 0 else 1
    }

    override fun evoluteEpoch(initPopulation: List<Individual>): List<Individual> {
        println("эпоха ${++curEpoch}")
        val start = System.nanoTime()
        population = super.evoluteEpoch(initPopulation)
        if (curEpoch % savePerEpoch == 0) saveNets()
        println(population.map { it.rate })
        val fin = System.nanoTime()
        println("Время: ${(fin-start)/1_000_000} мс\n")
        return population
    }

    fun saveNets() {
        println("saving...")
        with(NetworkIO()) {
            population.forEachIndexed {i, (nw, _) -> save(nw, "save$i.net") }
        }
    }
}

fun main(args: Array<String>) {
    with(EvolutionCheckers(20, 5, 20)) {
        val nw = evolute(20).nw
        saveNets()
        NetworkIO().save(nw, "best.net")
    }
}
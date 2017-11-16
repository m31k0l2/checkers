//class EvolutionCheckers(populationSize: Int, scale: Int): AbstractEvolution(populationSize, scale) {
//    val game = Game()
//
//    override fun play(a: Network, b: Network): Int {
//        val player1 = Player(a, 2)
//        val player2 = Player(b, 2)
//        val res = Game.play(player1, player2)
//        return 1.takeIf { res == 1 } ?: 0.takeIf { res == 0 } ?: -2
//    }
//
//    override fun createNet() = Network(91, 40, 10, 1)
//
//    override fun nextGeneration(): List<Individual> {
//        super.nextGeneration()
//        (0..populationSize-1).forEach {
//            population[it].nw.save("save$it.net")
//        }
//        return population
//    }
//
//    override fun evolute(count: Int) {
//        population = generatePopulation(populationSize)
//        population = competition(population)
//        var start = System.nanoTime()
//        for (i in 0..count-1) {
//            println()
//            println(population.take(15))
//            val fin = System.nanoTime()
//            println("time: ${(fin-start)/1_000_000} мс")
//            println("step: $i")
//            println("mutantRate: $mutantRate")
//            println("crossoverRate: $crossoverRate")
//            start = fin
//            nextGeneration()
//            mutantRate = random.nextDouble()*0.5
//            crossoverRate = random.nextDouble()
//            print("--")
//        }
//        println()
//    }
//
//    override fun generatePopulation(size: Int): List<Individual> {
//        if (!File("save0.net").exists()) return super.generatePopulation(size)
//        return (0..size-1).map { Individual(Network.load("save$it.net")!!) }
//    }
//}
//
//fun main(args: Array<String>) {
//    val ev = EvolutionCheckers(30, 5)
//    ev.evolute(100000)
//    ev.population.first().nw.save()
//    ev.population.forEach { println(it.rate) }
//}

fun play(player1: Player, player2: Player) {
    val game = Game()
    var moves: List<String>
    while (true) {
        game.print()
        if (game.currentColor == 0) {
            println("Ход белых:")
        } else println("Ход чёрных:")
        moves = game.nextMoves()
        moves.forEachIndexed { index, move ->
            println("${index + 1}) $move")
        }
        if (moves.isEmpty()) {
            if (game.currentColor == 0) println("чёрные победили")
            else println("белые победили")
            return
        }
        val player = player1.takeIf { game.currentColor == 0 } ?: player2
        game.go(player.selectMove(game.checkerboard, game.currentColor, moves))
        game.currentColor = 1 - game.currentColor
    }
}

fun createNet() = Network(91, 40, 10, 1)

fun main(args: Array<String>) {
    val player1 = Player(createNet())
    val player2 = Player(createNet())
    play(player1, player2)
}
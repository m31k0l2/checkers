import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

val stepLimit = 20
val testGames = 1

fun step(game: GameController, player: Player, moves: List<String>) {
    val step = player.selectMove(game.checkerboard, game.currentColor, moves)
    game.go(step)
    game.currentColor = 1 - game.currentColor
}

fun play(nw1: Network, nw2: Network, error: Double = 0.0, debug: Boolean = false): Int {
    val game = GameController()
    var moves: List<String>
    val white = Player(nw1, 2, error, debug)
    val black = Player(nw2, 2, error, debug)
    var curStep = 0
    while (curStep++ < stepLimit) {
        if (debug) game.print()
        moves = game.nextMoves()
        if (moves.isEmpty()) {
            return if (game.currentColor == 0) {
                1
            } else {
                0
            }
        }
        if (curStep == stepLimit) {
            return if (game.checkerboard.encodeToVector().sum() > 0) {
                0
            } else {
                1
            }
        }
        step(game, white.takeIf { game.currentColor == 0 } ?: black, moves)
    }
    return -1
}

fun play(net1: String, net2: String, error: Double = 0.0, debug: Boolean = false): String {
    val game = GameController()
    var moves: List<String>
    val nw1 = NetworkIO().load(net1)!!
    val nw2 = NetworkIO().load(net2)!!
    val white = Player(nw1, 2, error, debug)
    val black = Player(nw2, 2, error, debug)
    var curStep = 0
    while (curStep++ < stepLimit) {
        if (debug) game.print()
        moves = game.nextMoves()
        if (moves.isEmpty()) {
            return if (game.currentColor == 0) {
                if (debug) println("$net2 победила")
                net2
            } else {
                if (debug) println("$net1 победила")
                net1
            }
        }
        if (curStep == stepLimit) {
            return if (game.checkerboard.encodeToVector().sum() > 0) {
                if (debug) println("$net1 победила")
                net1
            } else {
                if (debug) println("$net2 победила")
                net2
            }
        }
        step(game, white.takeIf { game.currentColor == 0 } ?: black, moves)
    }
    return ""
}

suspend fun test(name1: String, name2: String, counter: AtomicInteger, netsSize: Int): Double {
    val netNames = listOf(name1, name2)
    val score1 = AtomicInteger()
    val score2 = AtomicInteger()
    val jobs = List(testGames) {
        launch {
            //            val k = Random().nextInt(2)
            (0..1).forEach {
                val winner = play(netNames[it], netNames[1 - it])
                if (winner == netNames[0]) score1.incrementAndGet()
                else score2.incrementAndGet()
                val step = counter.incrementAndGet()
                val total = step * 100 / (netsSize * (netsSize - 1) * testGames * 2)
                println("$total%")
            }
        }
    }
    jobs.forEach { it.join() }
    val total = score1.get() + score2.get()
    val result = score1.get() * 100.0 / total
    return result
}

fun testNets(nets: List<String>): String {
    val counter = AtomicInteger(0)
    return runBlocking {
        val results =
                nets.map { curNet ->
                    async {
                        var result = 0.0
                        nets.forEach { net ->
                            if (net != curNet) {
                                result += test(curNet, net, counter, nets.size)
                            }
                        }
                        curNet to result
                    }
                }

        results.map { it.await() }.mapIndexed { index, pair ->
            if (index == 0) println("~~~")
            println("${pair.first}: ${Math.round(pair.second / (nets.size - 1))}%")
            return@mapIndexed pair.first to Math.round(pair.second / (nets.size - 1))
        }.maxBy { it.second }!!.first
    }
}

fun main(args: Array<String>) {
    val myFolder = File("nets/winners")
    val files = myFolder.listFiles()
    val nets = files.map { "nets/winners/${it.name}"}
    testNets(nets)
}
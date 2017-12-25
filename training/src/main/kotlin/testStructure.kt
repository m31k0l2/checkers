import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

val stepLimit = 50

fun step(game: GameController, player: Player, moves: List<String>) {
    val step = player.selectMove(game.checkerboard, game.currentColor, moves)
    game.go(step)
    game.currentColor = 1 - game.currentColor
}

fun play(net1: String, net2: String, debug: Boolean = false): String {
    val game = GameController()
    var moves: List<String>
    val nw1 = NetworkIO().load("$net1.net")!!
    val nw2 = NetworkIO().load("$net2.net")!!
    val white = Player(nw1, debug = debug)
    val black = Player(nw2, debug = debug)
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

fun test() {
    val nets = mutableMapOf<String, Int>()
    val netNames = listOf("60-40-20-10", "40-20")
    nets.put(netNames[0], 0)
    nets.put(netNames[1], 0)
    for (k in 0..1) {
        for (i in 1..10) {
            val winner = play(netNames[k], netNames[1 - k])
            nets[winner] = nets[winner]!! + 1
            print("$i.")
        }
        println()
    }
    nets.forEach { println(it) }
}

suspend fun test2(size: Int = 10) {
    val netNames = listOf("best", "40-20")
    val score1 = AtomicInteger()
    val score2 = AtomicInteger()
    val jobs = List(size) {
        launch {
            val k = Random().nextInt(2)
            val winner = play(netNames[k], netNames[1 - k])
            if (winner == netNames[0]) score1.incrementAndGet()
            else score2.incrementAndGet()
            print(".")
        }
    }
    jobs.forEach { it.join() }
    println()
    val total = score1.get() + score2.get()
    println("${netNames.first()}: ${score1.get() * 100.0 / total}%")
    println("${netNames.last()}: ${score2.get() * 100.0 / total}%")
}

fun main(args: Array<String>) = runBlocking {
    test2(15)
}
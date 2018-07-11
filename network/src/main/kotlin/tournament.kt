import kotlin.streams.toList

fun playOff(playOffGroups: List<List<Individual>>) = playOffGroups.parallelStream().map { (white, black) ->
    val res = game(white.nw, black.nw)
    print("#")
    when (res) {
        0 -> {
            white.rate += black.rate + 1
            white
        }
        1 -> {
            black.rate += white.rate + 1
            black
        }
        else -> {
            if (black.rate > white.rate) black else white
        }
    }
    }.toList().chunked(2)

fun tournament(networks: Set<Network>): List<Individual> {
    val groups = networks.chunked(4)
    val next = groups.map { groupGames(it[0], it[1], it[2], it[3]) }.flatten()
    val players = next.map { (nw, rate) -> Individual(nw, rate) }.shuffled()
    var playOffGroups = players.chunked(2)
    do {
        playOffGroups = playOff(playOffGroups)
    } while (playOffGroups.size != 1)
    val winner = playOffGroups.flatten().first()
    println(winner.nw.layers.map { it.neurons.size })
    winner.nw.save("nets/win.net")
    return players.sortedBy { it.rate }.reversed()
}

fun main(args: Array<String>) {
    tournament(List(32) { buildNetwork(3, 3, 3, 10, 1) }.toSet())
}

fun groupGames(vararg players: Network): List<Pair<Network, Int>> {
    if (players.size != 4) throw Exception("Группа должна состоять из четырёх игроков")
    val a = players[0]
    val b = players[1]
    val c = players[2]
    val d = players[3]
    val group = mapOf(a to mutableListOf<Int>(), b to mutableListOf(), c to mutableListOf(), d to mutableListOf())
    fun play(nw1: Network, nw2: Network) {
        val (white, black) = listOf(nw1, nw2).shuffled()
        val result = game(white, black)
        when (result) {
            -1 -> {
//                println("Ничья")
                group[white]!!.add(0)
                group[black]!!.add(0)
            }
            0 -> {
//                println("Победил белый игрок")
                group[white]!!.add(1)
                group[black]!!.add(-1)
            }
            else -> {
//                println("Победил черный игрок")
                group[white]!!.add(-1)
                group[black]!!.add(1)
            }
        }
    }
    val pairs = listOf(a to b, a to c, a to d, b to c, b to d, c to d)
    pairs.parallelStream().forEach { (a, b) -> play(a, b); print(".") }
    println()
    val table = group.map { it.key to it.value.sum() }.sortedBy { it.second }
    return table.takeLast(2)
}

private fun game(white: Network, black: Network): Int {
    var state: Checkerboard? = GameController().checkerboard
//    state?.print()
    val limit = 100
    for (i in 0..limit) {
        val color = i % 2
        state = if (color == 0) white(state!!, white) else black(state!!, black)
        if (state == null) {
            return 1 - color
        }
//        if (color == 0) println("Ход белых") else println("Ход черных")
//        state.print()
    }
    return -1
}

fun white(state: Checkerboard, white: Network) = agent(state, 0, white)

fun black(state: Checkerboard, black: Network) = agent(state, 1, black)

fun agent(state: Checkerboard, color: Int, nw: Network): Checkerboard? {
    val nodes = alphaBetaSearch(Node(state, color = color, nw = nw)) ?: return null
    val pairs = nodes.map { it to nw.activate(it.state, 1.0) }
    val (node, _) = if (color == 0) pairs.maxBy { it.second }!! else pairs.minBy { it.second }!!
    return node.state
}
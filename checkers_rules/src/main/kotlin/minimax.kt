import kotlin.math.max
import kotlin.math.min

var agentColor = 0

data class Node(val state: Checkerboard, val color: Int=0, val parent: Node?=null, var action: String="") {
    val level: Int = if (parent == null) 0 else parent.level + 1
    val children = lazy { makeChildren() }
    var value = state.value(agentColor)

    private fun makeChildren(): List<Node> {
        val gc = GameController(state).apply { currentColor = color }
        return gc.nextMoves().map { Node(nextState(it), 1 - color, this, it) }
    }
    private fun nextState(move: String): Checkerboard {
        val gc = GameController(state.clone())
        gc.go(move)
        return gc.checkerboard
    }
    private fun Checkerboard.h(): Int {
        val values = (0..1).map { value ->
            val checkers = getCheckers(value).map { get(it)!!.checker!! }
            checkers.asSequence().filter { it.type == 0 }.count() + checkers.asSequence().filter { it.type == 1 }.count() * 3
        }
        return values[agentColor] - values[1-agentColor]
    }

    private fun Checkerboard.value(color: Int): Int {
        val v = h()
        return if (color == agentColor) v else -v
    }

    override fun toString() = "$action -> $value"
}

var counter = 0
fun alphaBetaSearch(state: Node): List<Node>? {
    agentColor = state.color
    val v = maxValue(state, -1000, 1000)
    println("$v")
    val states = state.children.value.filter { it.value == v }
    if (states.isEmpty()) return null
    return states
}

fun terminalTest(node: Node) = node.level == 6

fun maxValue(state: Node, a: Int, beta: Int): Int {
    counter++
    var alpha = a
    if (terminalTest(state)) return state.value
    var v = -1000
    state.children.value.forEach {
        it.value = minValue(it, alpha, beta)
        v = max(v, it.value)
        if (v > beta) return v
        alpha = max(alpha, v)
    }
    return v
}

fun minValue(state: Node, alpha: Int, b: Int): Int {
    counter++
    var beta = b
    if (terminalTest(state)) return state.value
    var v = 1000
    state.children.value.forEach {
        it.value = maxValue(it, alpha, beta)
        v = min(v, it.value)
        if (v < alpha) return v
        beta = min(beta, v)
    }
    return v
}

fun main(args: Array<String>) {
    val checkerboard = Checkerboard()
    checkerboard.init(listOf("e1", "e7", "g1", "g3", "h4"), listOf("a5", "b8", "c3", "g7", "h2"))
    val gc = GameController(checkerboard)
    gc.print()
    var root = Node(gc.checkerboard)
    fun go(move: String) {
        gc.go(move)
        gc.currentColor = 1 - gc.currentColor
        root = Node(gc.checkerboard, gc.currentColor)
        gc.print()
        println(alphaBetaSearch(root))
    }
    var state: Node
    for (i in 0..6) {
        state = alphaBetaSearch(root)!!.first()
        println(state.action)
        go(state.action)
    }
}


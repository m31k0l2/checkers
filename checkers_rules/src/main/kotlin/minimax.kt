import kotlin.math.max
import kotlin.math.min

var agentColor = 0

data class Node(val state: Checkerboard, val parent: Node?=null, val color: Int=0, var action: String="") {
    val level: Int = if (parent == null) 0 else parent.level + 1
    val children = lazy { makeChildren() }
    var value = state.value(agentColor)

    private fun makeChildren(): List<Node> {
        val gc = GameController(state).apply { currentColor = color }
        return gc.nextMoves().map { Node(nextState(it), this, 1 - color, it) }
    }
    private fun nextState(move: String): Checkerboard {
        val gc = GameController(state.clone())
        gc.go(move)
        return gc.checkerboard
    }
    private fun Checkerboard.value(color: Int): Int {
        val values = (0..1).map {
            val checkers = getCheckers(it).map { get(it)!!.checker!! }
            checkers.filter { it.type == 0 }.count() + checkers.filter { it.type == 1 }.count() * 3
        }
        return values[color] - values[1-color]
    }

    override fun toString() = "$action -> $value"
}

var counter = 0
fun alphaBetaSearch(state: Node): Node? {
    agentColor = state.color
    val v = maxValue(state, Int.MIN_VALUE, Int.MAX_VALUE)
    val states = state.children.value.filter { it.value == v }
    if (states.isEmpty()) return null
    return states.shuffled().first()
}

fun terminalTest(node: Node) = node.level == 4

fun maxValue(state: Node, a: Int, beta: Int): Int {
    counter++
    var alpha = a
    if (terminalTest(state)) return state.value
    var v = Int.MIN_VALUE
    state.children.value.forEach {
        it.value = minValue(it, alpha, beta)
        v = max(v, it.value)
        if (v >= beta) return v
        alpha = max(alpha, v)
    }
    return v
}

fun minValue(state: Node, alpha: Int, b: Int): Int {
    counter++
    var beta = b
    if (terminalTest(state)) return state.value
    var v = Int.MAX_VALUE
    state.children.value.forEach {
        it.value = maxValue(it, alpha, beta)
        v = min(v, it.value)
        if (v <= alpha) return v
        beta = min(beta, v)
    }
    return v
}
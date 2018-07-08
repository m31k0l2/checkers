fun main(args: Array<String>) {
    var state: Checkerboard? = GameController().checkerboard
    state?.print()
    val limit = 100
    for (i in 0..limit) {
        val color = i % 2
        state = agent(state!!, color)
        if (state == null) {
            println("Победил ${1-color}")
            break
        }
        if (color == 0) println("Ход белых") else println("Ход черных")
        state.print()
        if (i == limit) {
            println("Ничья")
        }
    }
}

fun agent(state: Checkerboard, color: Int): Checkerboard? {
    val node = alphaBetaSearch(Node(state, color = color))
    println(node?.value)
    println(node?.action)
    return node?.state
}

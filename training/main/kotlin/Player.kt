import kotlin.streams.toList

class Player(private val nw: Network, private val predicateMoves: Int = 4, private val debug: Boolean = false) {
    fun selectMove(checkerboard: Checkerboard, color: Int, steps: List<String>): String {
        return steps.parallelStream().map { it to play(checkerboard, color, predicateMoves, it) }.toList()
                .onEach { if (debug) println(it) }
                .maxBy { it.second }!!.first
                .also { if (debug) println(it) }
    }

    private fun selectBestStep(checkerboard: Checkerboard, color: Int, steps: List<String>): String {
        if (steps.isEmpty()) return ""
        if (steps.size == 1) return steps[0]
        val list = steps.map { it to Game(checkerboard.clone()) }.map { (command, game) ->
            game.go(command)
            val vector = game.checkerboard.encodeToVector()
            val o = nw.multiActivate(InputEncoder().encode(vector))
            command to o[0]
        }
        return (if (color == 0) {
            list.maxBy { it -> it.second }!!.first
        } else list.minBy { it.second }!!.first)
    }

    fun play(checkerboard: Checkerboard, initColor: Int, count: Int, initStep: String): Int {
        val game = Game(checkerboard.clone())
        game.currentColor = initColor
        var steps = game.nextMoves()
        for (i in 0 until count * 2) {
            val step = if (i == 0) initStep else selectBestStep(game.checkerboard, game.currentColor, steps)
            game.go(step)
            game.currentColor = 1 - game.currentColor
            steps = game.nextMoves()
            if (steps.isEmpty()) {
                return if (game.currentColor != initColor) 100
                else -100
            }
        }
        val whiteCount = game.checkerboard.encodeToVector().filter { it > 0 }.count()
        val blackCount = game.checkerboard.encodeToVector().filter { it < 0 }.count()
        return if (initColor == 0) whiteCount - blackCount
        else blackCount - whiteCount
    }
}
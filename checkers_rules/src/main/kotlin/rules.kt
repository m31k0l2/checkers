class Game {
    private val board = CheckersBoard()
    var currentColor = 0
    init {
        val whiteCheckers = listOf("a1", "c1", "e1", "g1", "b2", "d2", "f2", "h2", "a3", "c3", "e3", "g3")
        val blackCheckers = listOf("b8", "d8", "f8", "h8", "a7", "c7", "e7", "g7", "b6", "d6", "f6", "h6")
        init(whiteCheckers, blackCheckers)
    }

    fun print() = board.print()

    private fun move(from: String, to: String) {
        board.move(from, to)
    }

    private fun kill(from: String, to: String) {
        val checker = board.get(from)?.checker
        board.remove(from, to)
        board.place(to, checker)
    }

    fun go(command: String) {
        val moveTemplate = Regex("([a-z]\\d)-([a-z]\\d)")
        val killTemplate = Regex("([a-z]\\d):([a-z]\\d).*")
        moveTemplate.matchEntire(command)?.let {
            val from = it.groups[1]!!.value
            val to = it.groups[2]!!.value
            move(from, to)
            return
        }
        killTemplate.matchEntire(command)?.let {
            val positions = it.value.split(":")
            for (i in 1 until positions.size) {
                val from = positions[i-1]
                val to = positions[i]
                kill(from, to)
            }
        }
    }

    /**
     * Назначает дамку в позиции pos
     */
    fun queen(pos: String) {
        board.get(pos)?.checker?.type = 1
    }

    /**
     * Инициализация шашек на доске. Для уточнения дамок воспользоваться функцией queen
     */
    fun init(whiteCheckers: List<String>, blackCheckers: List<String>) {
        board.clear()
        whiteCheckers.forEach { board.place(it, BoardChecker(0)) }
        blackCheckers.forEach { board.place(it, BoardChecker(1)) }
    }

    fun nextMoves() = MoveSearcher(currentColor, board).nextMoves()
}
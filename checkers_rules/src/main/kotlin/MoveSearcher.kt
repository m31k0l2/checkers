/**
 * Предназначен для поиска всех возможных ходов в текущей конфигурации доски [board] для хода игрока, играющего за
 * [currentColor] (за белых или чёрных)
 */
class MoveSearcher(private val currentColor: Int, private val board: CheckersBoard) {
    private lateinit var startPosition: BoardPosition
    private var killerType = 0
    private lateinit var victims: List<BoardPosition>

    /**
     * Описание перемещения хода
     * Каждое следующее перемещение хранит историю предыдущего перемещения.
     * Реализивано по типу связного списка.
     */
    data class Move(val from: Move?, val victim: BoardPosition?, val to: BoardPosition) {
        fun toList(): List<BoardPosition> {
            val list = mutableListOf<BoardPosition>()
            list.add(to)
            var move = from
            while (true) {
                move?.let {
                    list.add(it.to)
                    move = it.from
                } ?: break
            }
            list.reverse()
            return list
        }
    }

    /**
     * Возвращает список ходов
     */
    fun nextMoves(): List<String> {
        val nextColor = 1 - currentColor
        victims = board.getCheckers(nextColor)
        val killers = board.getCheckers(currentColor)
        var moves = killers.mapNotNull {
            startPosition = it
            killerType = board.get(it)!!.checker!!.type
            getKillerMoves(it).forEach {
                val list = it.toList()
                if (list.size > 1) return@mapNotNull it.toList()
                else return@mapNotNull null
            }
            return@mapNotNull null
        }.map { it.map{it.toString()}.reduce { acc, pos -> "$acc:$pos"} }
        if (moves.isEmpty()) {
            moves = board.getCheckers(currentColor).mapNotNull { from ->
                findMoveWay(from)?.map { listOf(from, it) }
            }.flatMap { it }.map { it.map{it.toString()}.reduce { acc, pos -> "$acc-$pos"} }
        }
        return moves
    }

    private fun getNextPosition(pos: BoardPosition, dx: Int, dy: Int): BoardPosition? {
        val x = pos.x + dx
        val y = pos.y + dy
        if (y < 1 || y > 8 || x < 1 || x > 8) return null
        return BoardPosition(x, y)
    }

    private fun findMoveWay(pos: BoardPosition): List<BoardPosition>? {
        val checker = board.get(pos)!!.checker ?: return null
        return if (checker.type == 0) {
            val dy = if (checker.color == 0) 1 else -1
            listOf(-1, 1).mapNotNull { getNextPosition(pos, it, dy) }.mapNotNull { it -> if (board.get(it)?.checker == null) it else null }
        } else listOf(listOf(-1, 1), listOf(1, 1), listOf(-1, -1), listOf(1, -1)).map { (dx, dy) ->
            (1..8).mapNotNull { getNextPosition(pos, dx*it, dy*it) }
                    .takeWhile { it -> board.get(it)?.checker == null }
        }.flatMap { it }
    }

    private fun findNextPositions(killerPosition: BoardPosition, victimPosition: BoardPosition): List<BoardPosition> {
        var dx = victimPosition.x - killerPosition.x
        var dy = victimPosition.y - killerPosition.y
        if (killerType == 0) {
            val to = BoardPosition(killerPosition.x + 2 * dx, killerPosition.y + 2 * dy)
            if (to == startPosition) return listOf(to)
            val field = board.get(to) ?: return emptyList()
            field.checker?.let { return emptyList() }
            return listOf(to)
        } else {
            return (1..8).mapNotNull {
                dx = if (dx < 0) dx - 1 else dx + 1
                dy = if (dy < 0) dy - 1 else dy + 1
                val x = killerPosition.x + dx
                val y = killerPosition.y + dy
                return@mapNotNull if (x in (1..8) && y in (1..8)) {
                    BoardPosition(x, y)
                } else null
            }
        }
    }

    private fun isOneDiagonal(pos1: BoardPosition, pos2: BoardPosition) = Math.abs(pos1.x - pos2.x) == Math.abs(pos1.y - pos2.y)

    private fun isBetween(pos: BoardPosition, a: BoardPosition, b: BoardPosition): Boolean {
        if (!isOneDiagonal(pos, a) || !isOneDiagonal(pos, b) || !isOneDiagonal(a, b)) return false
        val dAB = Math.abs(b.x - a.x)
        val dA = Math.abs(pos.x - a.x)
        val dB = Math.abs(pos.x - b.x)
        return dAB >= dA && dAB >= dB
    }

    /** Поиск шашек, находящихся под ударом.
     * Для шашки - это рядом стоящая шашка
     * Для дамки - это шашки на одной диагонали
     **/
    private fun findVictims(victims: List<BoardPosition>, killerPosition: BoardPosition) = when (killerType) {
        0 -> victims.filter { Math.abs(killerPosition.x - it.x) == 1 && Math.abs(killerPosition.y - it.y) == 1 }
        else -> victims.filter { isOneDiagonal(killerPosition, it) }
    }

    private fun isCheckersBetween(a: BoardPosition, b: BoardPosition): Boolean {
        val dx = 1.takeIf { a.x - b.x < 0 } ?: -1
        val dy = 1.takeIf { a.y - b.y < 0 } ?: -1
        val x1 = Math.min(a.x, b.x)+1
        val x2 = Math.max(a.x, b.x)-1
        val y1 = Math.min(a.y, b.y)+1
        val y2 = Math.max(a.y, b.y)-1
        return (1 until 8)
                .map { BoardPosition(a.x + dx * it, a.y + dy * it) }
                .takeWhile { (Math.abs(it.x) in (x1..x2) && Math.abs(it.y) in (y1..y2)) }
                .mapNotNull { board.get(it)?.checker }
                .any()
    }

    /**
     * Выдаёт список битых шашек за текущий ход.
     * Т.к. класс Move реалезован в виде связного списка, элементы которого хранят информацию о начале и конце хода,
     * а также о снятых шашек, то для поиска всех битых шашек надо просто в цикле отмотать ходы до первого и собрать
     * эту информацию в список killed
     */
    private fun getKilledInMove(move: Move): List<BoardPosition> {
        val killed = mutableListOf<BoardPosition>()
        var curMove = move
        while (true) {
            curMove.victim?.let { killed.add(it) }
            curMove = curMove.from ?: break
        }
        return killed
    }

    private fun getNearbyVictims(killerPosition: BoardPosition, killed: List<BoardPosition>): List<BoardPosition> {
        return findVictims(victims, killerPosition).filter { !killed.contains(it) }.filter {
            with(killerPosition) {
                val dx = it.x - x
                val dy = it.y - y
                val xAfter = x + if (dx < 0) dx - 1 else dx + 1
                val yAfter = y + if (dy < 0) dy - 1 else dy + 1
                val xBefore = x + if (dx < 0) dx + 1 else dx - 1
                val yBefore = y + if (dy < 0) dy + 1 else dy - 1
                val after = BoardPosition(xAfter, yAfter)
                val before = BoardPosition(xBefore, yBefore)
                val checkerAfter = board.get(after)?.checker
                val checkerBefore = board.get(before)?.checker
                (after == startPosition || checkerAfter == null) && (before == killerPosition || checkerBefore == null)
            }
        }
    }

    private fun getKillerMoves(initMove: Move, allMoves: MutableList<Move>) {
        var move = initMove
        while (true) {
            val killer = move.to // текущее положение атакующей шашки
            if (killerType == 0) {
                if ((move.to.y == 1 && currentColor == 1) || (move.to.y == 8 && currentColor == 0)) killerType = 1
            }
            val killed = getKilledInMove(move) // список битых за удар шашек
            // список шашек, находящихся под ударом с позиции killed
            val nearbyVictims = getNearbyVictims(killer, killed).filter { !isCheckersBetween(killer, it) }
            if (nearbyVictims.isEmpty()) break
            val nextMoves = nearbyVictims.mapNotNull { victim ->
                if (killed.any { isBetween(it, killer, victim) }) return@mapNotNull null
                val nextPositions = findNextPositions(killer, victim).takeWhile {
                    board.get(it)?.checker?.color != currentColor || it == startPosition
                }
                if (nextPositions.isEmpty()) return@mapNotNull null
                val testPositions = nextPositions.mapNotNull {
                    if (getNearbyVictims(it, killed).any { it != victim }) it else null
                }
                (nextPositions.takeIf { testPositions.isEmpty() } ?: testPositions).filter { next -> !killed.filter { isBetween(it, killer, next) }.any() }.map { Move(move, victim, it) }
            }.flatMap { it }
            if (nextMoves.isEmpty()) break
            if (nextMoves.size > 1) {
                nextMoves.forEach {
                    getKillerMoves(it, allMoves)
                }
                return
            }
            move = nextMoves.first()
        }
        allMoves.add(move)
    }

    private fun getKillerMoves(attackPosition: BoardPosition): List<Move> {
        val moves = mutableListOf<Move>()
        getKillerMoves(Move(null, null, attackPosition), moves)
        return moves
    }
}
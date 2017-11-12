data class BoardChecker(val color: Int, var type: Int=0)

data class BoardField(val x: Int, val y: Int, val color: Int, var checker: BoardChecker? = null)

class BoardPosition(val x: Int, val y: Int) {
    constructor(s: String) : this(s.getX(), s.getY())

    override fun toString(): String {
        val ch = ('a' + x - 1).toString()
        return "$ch$y"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BoardPosition
        return x == other.x && y == other.y
    }

    override fun hashCode() = 31 * x + y
}

private fun String.getX() = ('a'..'h').map { it.toString() to it.toInt() - 96 }.toMap()[substring(0, 1)]!!

private fun String.getY() = substring(1, 2).toInt()

class CheckersBoard {
    private val board: List<BoardField>

    init {
        board = (1..8).flatMap { y -> (1..8).map { x -> BoardField(x, y, (x + y + 1) % 2 ) } }
    }

    private fun get(x: Int, y: Int) = board.find { it.x == x && it.y == y }

    fun get(pos: BoardPosition) = get(pos.x, pos.y)

    fun get(pos: String) = get(BoardPosition(pos))

    fun print() {
        (8 downTo 1).forEach { y ->
            print("$y| ")
            (1..8).forEach { x ->
                val field = get(x, y)!!
                val checker = with(field) {
                    when {
                        checker?.color == 0 && checker?.type == 0 -> "o"
                        checker?.color == 0 && checker?.type == 1 -> "@"
                        checker?.color == 1 && checker?.type == 0 -> "x"
                        checker?.color == 1 && checker?.type == 1 -> "#"
                        else -> " "
                    }
                }
                print("   ".takeIf { field.color == 0 } ?: "[$checker]")
            }
            println()
        }
        println("    a  b  c  d  e  f  g  h")
    }

    private fun place(x: Int, y: Int, checker: BoardChecker?) {
        if (checker?.type == 0 && ((y == 8 && checker.color == 0) || (y == 1 && checker.color == 1))) checker.type = 1
        get(x, y)!!.checker = checker
    }

    private fun place(pos: BoardPosition, checker: BoardChecker?) = place(pos.x, pos.y, checker)

    fun place(pos: String, checker: BoardChecker?) = place(BoardPosition(pos), checker)

    fun move(from: String, to: String) {
        val checker = get(from)!!.checker
        place(from, null)
        place(to, checker)
    }

    private fun remove(x: Int, y: Int)= place(x, y, null)

    fun remove(pos: BoardPosition)= place(pos, null)

    fun remove(pos: String)= place(pos, null)

    private fun remove(from: BoardPosition, to: BoardPosition) {
        val x1 = Math.min(from.x, to.x)
        val x2 = Math.max(from.x, to.x)
        val y1 = Math.min(from.y, to.y)
        val y2 = Math.max(from.y, to.y)
        (x1..x2).forEach { x -> (y1..y2).forEach { y -> remove(x, y) } }
    }

    fun remove(from: String, to: String) = remove(BoardPosition(from), BoardPosition(to))
    fun getCheckers(color: Int) = board.filter {
        it.checker != null && it.checker!!.color == color }.map { BoardPosition(it.x, it.y) }

    fun clear() {
        board.forEach { it.checker = null }
    }
}

class MoveSearcher(private val currentColor: Int, private val board: CheckersBoard) {
    private lateinit var startPosition: BoardPosition
    private var killerType = 0
    private lateinit var victims: List<BoardPosition>

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
            listOf(-1, 1).mapNotNull { getNextPosition(pos, it, dy) }.takeWhile { it -> board.get(it)?.checker == null }
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
                .map { BoardPosition(a.x + dx* it, a.y + dy* it) }
                .takeWhile { (Math.abs(it.x) in (x1..x2) && Math.abs(it.y) in (y1..y2)) }
                .mapNotNull { board.get(it)?.checker }
                .any()
    }

    fun nextMoves(): List<List<BoardPosition>> {
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
        }
        if (moves.isEmpty()) {
            moves = board.getCheckers(currentColor).mapNotNull { from ->
                findMoveWay(from)?.map { listOf(from, it) }
            }.flatMap { it }
        }
        return moves
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

class Game {
    private val board = CheckersBoard()
    var currentColor = 0
    private lateinit var startPosition: BoardPosition
    private var killerType = 0
    private lateinit var victims: List<BoardPosition>
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
        val killTemplate = Regex("([a-z]\\d):([a-z]\\d)")
        moveTemplate.matchEntire(command)?.let {
            val from = it.groups[1]!!.value
            val to = it.groups[2]!!.value
            move(from, to)
            return
        }
        killTemplate.matchEntire(command)?.let {
            val from = it.groups[1]!!.value
            val to = it.groups[2]!!.value
            kill(from, to)
        }
    }

    fun remove(pos: String) = board.remove(pos)

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

//todo: поиск хода

fun main(args: Array<String>) {
    val game = Game()
    with(game) {
        test1()
        test2()
        test3()
        test4()
        test5()
        test6()
        test7()
        test8()
        test9()
        test10()
    }
}

private fun Game.test1() {
    println("Test 1")
    currentColor = 0
    val whiteCheckers = listOf("e1")
    val blackCheckers = listOf("d2", "b4", "b6")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test2() {
    println("Test 2")
    currentColor = 0
    val whiteCheckers = listOf("e1")
    val blackCheckers = listOf("d2", "c3", "b6")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test3() {
    println("Test 3")
    currentColor = 0
    val whiteCheckers = listOf("e1")
    val blackCheckers = listOf("d2", "d4", "f2", "f4")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test4() {
    println("Test 4")
    currentColor = 0
    val whiteCheckers = listOf("h2")
    val blackCheckers = listOf("b2", "f4", "g7")
    init(whiteCheckers, blackCheckers)
    queen("h2")
    print()
    println(nextMoves())
}

private fun Game.test5() {
    println("Test 5")
    currentColor = 1
    val whiteCheckers = listOf("f2", "c3", "e3", "d4", "f4")
    val blackCheckers = listOf("a5", "d6", "h6", "g7")
    init(whiteCheckers, blackCheckers)
    queen("a5")
    print()
    println(nextMoves())
}

private fun Game.test6() {
    println("Test 6")
    currentColor = 1
    val whiteCheckers = listOf("e1", "f2", "h2")
    val blackCheckers = listOf("c3", "e5", "b6", "d6", "a7", "e7")
    init(whiteCheckers, blackCheckers)
    queen("e1")
    queen("a7")
    print()
    println(nextMoves())
}

private fun Game.test7() {
    println("Test 7")
    currentColor = 0
    val whiteCheckers = listOf("e1", "f2", "h2")
    val blackCheckers = listOf("c3", "e5", "b6", "d6", "a7", "e7")
    init(whiteCheckers, blackCheckers)
    queen("e1")
    queen("a7")
    print()
    println(nextMoves())
}

private fun Game.test8() {
    println("Test 8")
    currentColor = 0
    val whiteCheckers = listOf("g1", "c1", "a1", "h2", "d2", "g3", "e3", "f4", "d4", "c5")
    val blackCheckers = listOf("h8", "d8", "b8", "g7", "e7", "c7", "a7", "b6", "g5")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test9() {
    println("Test 9")
    currentColor = 1
    val whiteCheckers = listOf("g1", "c1", "a1", "h2", "d2", "g3", "e3", "d4", "c5", "d6", "f6")
    val blackCheckers = listOf("h8", "d8", "b8", "c7", "a7", "b6")
    init(whiteCheckers, blackCheckers)
    queen("d6")
    print()
    println(nextMoves())
}

private fun Game.test10() {
    println("Test 10")
    currentColor = 0
    print()
    println(nextMoves())
}

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

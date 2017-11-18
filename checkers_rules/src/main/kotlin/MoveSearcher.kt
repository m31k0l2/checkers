/**
 * Предназначен для поиска всех возможных ходов в текущей конфигурации доски [board] для хода игрока, играющего за
 * [currentColor] (за белых или чёрных).
 * [victims] - шашки противника
 * [startPosition] - переменная в которой содержится информация о стартовой позиции рассматриваемой шашки
 * [killerType] - переменная в которой содержится информация о текущем типе рассматриваемой шашки (шашка или дамка)
 */
class MoveSearcher(private val currentColor: Int, private val board: Checkerboard) {
    private lateinit var startPosition: Position
    private var killerType = 0
    private val victims = board.getCheckers(1 - currentColor)

    /**
     * Описание перемещения хода
     * Каждое следующее перемещение хранит историю предыдущего перемещения.
     * Реализивано по типу связного списка.
     */
    data class Move(private val from: Move?, private val victim: Position?, val to: Position) {
        /**
         * Преобразует связный список Move в список позиций перемещений хода, следующего вида
         * [f4, h6, f8, d6], что означает с f4 переход на h6, с h6 на f8 и т.д.
         */
        fun toList(): List<Position> {
            val list = mutableListOf<Position>()
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

        /**
         * Выдаёт список битых шашек за текущий ход.
         * Т.к. класс Move реалезован в виде связного списка, элементы которого хранят информацию о начале и конце хода,
         * а также о снятых шашек, то для поиска всех битых шашек надо просто в цикле отмотать ходы до первого и собрать
         * эту информацию в список victims
         */
        fun getVictims(): List<Position> {
            val victims = mutableListOf<Position>()
            var curMove = this
            while (true) {
                curMove.victim?.let { victims.add(it) }
                curMove = curMove.from ?: break
            }
            return victims
        }
    }

    /**
     * Возвращает список ходов
     * Сначало определяются атакующие ходы, если таких нет то возвращаются ходы перемещения
     */
    fun nextMoves() = nextKillMoves().takeIf { it.isNotEmpty() } ?: nextStepMoves()

    /**
     * Возвращает список атакующих ходов в текстовой форме
     * Для каждой шашки ищем список атакующих ходов.
     * На выходе функции аккамулируем и возвращаем список всех возможных атак
     */
    private fun nextKillMoves() = board.getCheckers(currentColor).map {
        startPosition = it
        killerType = board.get(it)!!.checker!!.type
        getAttackPositionsList(it)
    }.flatMap { it }.filter { it.isNotEmpty() }.map { it.map { it.toString() }.reduce { acc, pos -> "$acc:$pos" } }

    /**
     * Получает список атакующих позиций для каждого атакующего хода для шашки с позициец [checkerPosition]
     */
    private fun getAttackPositionsList(checkerPosition: Position) =
            getKillerMoves(checkerPosition).mapNotNull { it.toList().takeIf { it.size > 1 } }

    /**
     * Функция обёртка, возвращает список атакующих ходов для позицией
     */
    private fun getKillerMoves(attackPosition: Position): List<Move> {
        val moves = mutableListOf<Move>()
        getKillerMoves(Move(null, null, attackPosition), moves)
        return moves
    }

    /**
     * Поиск атакующих ходов
     * Выполняется рекурсивно, ходы аккамулируются в передоваемом хранилище [allMoves]
     * Текущая позиция задаётся ходом [initMove], то что он начальный определяется установками хода initMove:
     * [from = null, to = attackPosition]
     */
    private fun getKillerMoves(initMove: Move, allMoves: MutableList<Move>) {
        var move = initMove
        while (true) {
            val killer = move.to // текущее положение атакующей шашки
            // если шашка достигла последнего поля, то переводим её в дамки
            if (killerType == 0 && ((killer.y == 1 && currentColor == 1) || (killer.y == 8 && currentColor == 0))) killerType = 1
            val killed = move.getVictims() // список битых за удар шашек
            // список шашек, находящихся под ударом с позиции killer
            val nearbyVictims = getNearbyVictims(killer, killed).filter { !isCheckersBetween(killer, it) }
            // если список пуст, то прерываем поиск
            if (nearbyVictims.isEmpty()) break
            // иначе находим список возвоможных атакующих перемещений шашки
            val nextStepMoves = nextStepsForMove(move, nearbyVictims, killed)
            // если последующие атаки невозможны (шашке невозможно по той или иной причине атокавать), то прерываем поиск
            if (nextStepMoves.isEmpty()) break
            // если количество атак более одной, то для каждой следующей повторяем поиск,
            // все атаки так или иначе аккамулируются в хранилище allMoves
            if (nextStepMoves.size > 1) {
                nextStepMoves.forEach { getKillerMoves(it, allMoves) }
                return // функцию необходимо прервать, чтобы не дублировать атаки
            }
            // иначе продолжим поиск последующих перемещений
            move = nextStepMoves.first()
        }
        // запоминаем ход в хранилище
        allMoves.add(move)
    }

    /** возвращает список возвоможных атакующих перемещений шашки
     * [move] - предыдущая атака
     * [nearbyVictims] - список позиций шашек, находящихся под ударом
     * [killed] - список битых шашек в течении хода
     *
     * Для каждой шашки, находящейся под ударом, находим список позиций, куда может перемещаться атакующая шашка
     * Затем из этого списка получаем атаки класса Move
     */
    private fun nextStepsForMove(move: Move, nearbyVictims: List<Position>, killed: List<Position>) =
            nearbyVictims.mapNotNull { victim ->
                getAfterAttackPositions(move.to, victim, killed)?.map { Move(move, victim, it) }
        }.flatMap { it }

    /** Находит позиции после атаки, проверяет атаки на соответствие правилам, исключает позици, которые нарушают правила
     * [killer] - позиция атакующей шашки
     * [victim] - позиция атакуемой шашки
     * [killed] - список битых шашек в течении хода
     */
    private fun getAfterAttackPositions(killer: Position, victim: Position, killed: List<Position>): List<Position>? {
        if (killed.any { isBetween(it, killer, victim) }) return null
        // находим список возможных позиций после атаки
        val nextPositions = findNextPositions(killer, victim).takeWhile {
            // правило: за битой шашкой должно быть свободное поле
            board.get(it)?.checker?.color != currentColor || it == startPosition
        }
        // если атакующую шашку после нападения некуда ставить, то шашку нельзя атаковать
        if (nextPositions.isEmpty()) return null
        // правило: надо встать на ту позицию, которая позволяет атаковать дальше
        val testPositions = nextPositions.mapNotNull {
            if (getNearbyVictims(it, killed).any { it != victim }) it else null
        }
        return (testPositions.takeIf { it.isNotEmpty() } ?: nextPositions).filter { next ->
            !killed.filter { isBetween(it, killer, next) }.any()
        }
    }

    /** Находит позиции шашки после атаки **/
    private fun findNextPositions(killerPosition: Position, victimPosition: Position): List<Position> {
        if (killerType == 0) { // если простая шашка
            findNextPositionForSimpleChecker(killerPosition, victimPosition)?.let { return listOf(it) }
            return emptyList()
        } else return findNextPositionsForQueen(killerPosition, victimPosition)
    }

    /**
     * Найти позицию, следующаю за атакой для простой шашки
     * [killerPosition] - атакующая позиция
     * [victimPosition] - атакуемая позиция
     * [to] - следующая позиция за атакой
     *
     * если позиция после атаки такая же, что и стартовая на ходе, то вернём,
     * иначе бы не прошёл фильтр по пустоте поля (т.к. поле ещё не обнулено), или если на поле за атакованной шашкой
     * есть другая шашка то вернуть пустой список иначе список с позицией
     */
    private fun findNextPositionForSimpleChecker(killerPosition: Position, victimPosition: Position): Position? {
        val dx = victimPosition.x - killerPosition.x
        val dy = victimPosition.y - killerPosition.y
        val to = killerPosition.next(2 * dx, 2 * dy) ?: return null
        return to.takeIf { it == startPosition || board.get(to)!!.checker == null }
    }

    /**
     * Найти позиции, следующаю за атакой для дамки
     * [killerPosition] - атакующая позиция
     * [victimPosition] - атакуемая позиция
     */
    private fun findNextPositionsForQueen(killerPosition: Position, victimPosition: Position): List<Position> {
        var dx = victimPosition.x - killerPosition.x
        var dy = victimPosition.y - killerPosition.y
        return (1..8).mapNotNull {
            dx = if (dx < 0) dx-- else dx++
            dy = if (dy < 0) dy-- else dy++
            killerPosition.next(dx, dy)
        }
    }

    /**
     * Возвращает список позиций шашек которые можно взять
     * Шашки фильтруются по принципу, не содержатся в битых на этом ходе и за и перед ней пустое поле
     */
    private fun getNearbyVictims(killerPosition: Position, killed: List<Position>) =
            findVictims(victims, killerPosition).filter { !killed.contains(it) }.filter { checkChecker(killerPosition, it) }

    private fun isCheckersBetween(a: Position, b: Position): Boolean {
        val dx = 1.takeIf { a.x - b.x < 0 } ?: -1
        val dy = 1.takeIf { a.y - b.y < 0 } ?: -1
        val x1 = Math.min(a.x, b.x)+1
        val x2 = Math.max(a.x, b.x)-1
        val y1 = Math.min(a.y, b.y)+1
        val y2 = Math.max(a.y, b.y)-1
        return (1 until 8)
                .map { Position(a.x + dx * it, a.y + dy * it) }
                .takeWhile { (Math.abs(it.x) in (x1..x2) && Math.abs(it.y) in (y1..y2)) }
                .mapNotNull { board.get(it)?.checker }
                .any()
    }

    /** Поиск шашек, находящихся под ударом.
     * Для шашки - это рядом стоящая шашка
     * Для дамки - это шашки на одной диагонали
     **/
    private fun findVictims(victims: List<Position>, killerPosition: Position) = when (killerType) {
        0 -> victims.filter { Math.abs(killerPosition.x - it.x) == 1 && Math.abs(killerPosition.y - it.y) == 1 }
        else -> victims.filter { isOneDiagonal(killerPosition, it) }
    }

    /**
     * Проверка что шашка с позиции Killer может взять шашку с позицией victim, проверка, что нет шашек до и после victim
     */
    private fun checkChecker(killer: Position, victim: Position) = with(victim) {
        val dx = x - killer.x
        val dy = y - killer.y
        val xAfter = x + if (dx < 0) 1 else -1
        val yAfter = y + if (dy < 0) 1 else -1
        val xBefore = x + if (dx < 0) -1 else 1
        val yBefore = y + if (dy < 0) -1 else 1
        val after = Position(xAfter, yAfter)
        val before = Position(xBefore, yBefore)
        val checkerAfter = board.get(after)?.checker
        val checkerBefore = board.get(before)?.checker
        (after == startPosition || checkerAfter == null) && (before == killer || checkerBefore == null)
    }

    /**
     * Проверка, что шашки с позицией [pos1] и с позицией [pos2] на одной диагонали
     */
    private fun isOneDiagonal(pos1: Position, pos2: Position) = Math.abs(pos1.x - pos2.x) == Math.abs(pos1.y - pos2.y)

    /** Проверка, что шашка с позицией [pos] надодится между шашками с позициями [a] и [b] */
    private fun isBetween(pos: Position, a: Position, b: Position): Boolean {
        if (!isOneDiagonal(pos, a) || !isOneDiagonal(pos, b) || !isOneDiagonal(a, b)) return false
        val dAB = Math.abs(b.x - a.x)
        val dA = Math.abs(pos.x - a.x)
        val dB = Math.abs(pos.x - b.x)
        return dAB >= dA && dAB >= dB
    }

    /** Возвращает список всех возможных перемещений (не атак) шашек **/
    private fun nextStepMoves() = board.getCheckers(currentColor).mapNotNull { from ->
        findMoveWay(from)?.map { listOf(from, it) }
    }.flatMap { it }.map { it.map { it.toString() }.reduce { acc, pos -> "$acc-$pos" } }

    /** Возвращает список всех возможных перемещений шашки с позицией [pos]
     * Отдельно определяется для простой шашки и отдельно для дамки
     * Для шашки - это соседние клетки, на которых не других шашек
     * Для дамки - это две её диагонали, ходы берутся до тех пор, пока не будет встречена занятая клетка
     **/
    private fun findMoveWay(pos: Position): List<Position>? {
        val checker = board.get(pos)!!.checker ?: return null
        return if (checker.type == 0) {
            val dy = if (checker.color == 0) 1 else -1
            listOf(-1, 1).mapNotNull { pos.next(it, dy) }.mapNotNull { it -> if (board.get(it)?.checker == null) it else null }
        } else listOf(listOf(-1, 1), listOf(1, 1), listOf(-1, -1), listOf(1, -1)).map { (dx, dy) ->
            (1..8).mapNotNull { pos.next(dx * it, dy * it) }.takeWhile { it -> board.get(it)?.checker == null }
        }.flatMap { it }
    }
}
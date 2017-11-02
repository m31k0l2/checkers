class StepSearcher(val checkerboard: Checkerboard, val color: Int) {
    val steps get() = getAvailableKillSteps().takeIf { it.isNotEmpty() } ?: getAvailableMoveSteps()

    private val commandChains = mutableListOf<List<String>>()

    private fun getAvailableMoveSteps(): MutableList<String> {
        val steps = mutableListOf<String>()
        checkerboard.getCheckers(color).forEach { (color, field, queen) ->
            val position = field!!.position
            if (!queen) {
                val directions =
                        if (color == Color.WHITE) listOf(Direction.NW, Direction.NE)
                        else listOf(Direction.SW, Direction.SE)
                directions.forEach { position.moveByDirection(it)?.let {
                    checkerboard.getField(it).checker ?: steps.add("$position-$it")
                } }
            } else {
                getDiagonals(position).forEach { row ->
                    val fieldList = row.map { checkerboard.getField(it) }
                    if (fieldList.map { it.checker }.filterNotNull().isEmpty()) {
                        steps.addAll(fieldList.map { "$position-$it" })
                    } else {
                        steps.addAll(fieldList.takeWhile { it.checker == null }.map { "$position-$it" })
                    }
                }
            }
        }
        return steps
    }

    private fun getAvailableKillSteps(): List<String> {
        commandChains.clear()
        calculateForCheckers()
        calculateForQueens()
        val steps = commandChains.map {
            it.mapIndexed { i, command -> command.takeIf { i == 0 } ?: command.substring(2, 5) }.reduce { s1, s2 -> s1 + s2 }
        }
        return steps
    }

    private fun findKillMovesForChecker(virtualBoard: Checkerboard, checker: Checker): MutableList<String> {
        val moves = mutableListOf<String>()
        val position = checker.field!!.position
        for (dir in gameConstants.directions) {
            val pos = position.moveByDirection(dir) ?: continue
            val field = virtualBoard.getField(pos)
            val victim = field.checker ?: continue
            if (victim.color == color) continue
            val nextPos = pos.moveByDirection(dir) ?: continue
            val nextField = virtualBoard.getField(nextPos)
            nextField.checker ?: moves.add("$position:$nextPos")
        }
        return moves
    }

    private fun findKillMovesForQueen(board: Checkerboard, checker: Checker): List<String> {
        val finalMoves = mutableListOf<String>()
        val diagonals = getDiagonals(checker.field!!.position)
        diagonals.filter { hasVictimForQueen(board, it) }.map {
            val position = it.map { board.getField(it) }.filter { it.checker != null }.first().position
            val subList = it.subList(it.indexOf(position), it.size)
            val fields = mutableListOf(subList[0])
            fields += (1..subList.size-1)
                    .map { subList[it] }
                    .takeWhile { board.getField(it).checker == null }
            fields
        }.forEach {
            val victim = it.first()
            val attackMoves = mutableListOf<String>()
            val moves = mutableListOf<String>()
            for (i in 1..it.size-1) {
                board.clone().apply {
                    remove(victim.strPosition)
                    val next = it[i].strPosition
                    val queen = move(checker.field!!.position.strPosition, next).apply { queen = true }
                    if (findKillMovesForQueen(this, queen).isNotEmpty()) {
                        attackMoves.add(next)
                    }
                    moves.add(next)
                }
            }
            finalMoves.addAll((if (attackMoves.isNotEmpty()) attackMoves else moves).map { "${checker.field!!.position}:$it" })
        }
        return finalMoves
    }

    private fun hasVictimForQueen(virtualBoard: Checkerboard, diagonal: List<Position>): Boolean {
        var hasVictim = false
        diagonal.map { virtualBoard.getField(it) }.map { it.checker }.forEach {
            if (it != null) {
                if (hasVictim) return false
                if (it.color != color) hasVictim = true
                else return false
            } else if (hasVictim) return true
        }
        return false
    }

    private fun calculateForCheckers() {
        checkerboard.getCheckers(color).filter { !it.queen }.forEach { moveChecker(checkerboard, it) }
    }

    private fun moveChecker(board: Checkerboard, checker: Checker, command: String="", commandChain: MutableList<String> = mutableListOf()) {
        val killer = if (command.isBlank()) checker else {
            actions.kill(board, command).also {
                if (it.queen) {
                    moveQueen(board.clone(), it, "", commandChain)
                    return
                }
            }
        }
        val moves = findKillMovesForChecker(board, killer)
        if (moves.isEmpty() && commandChain.isNotEmpty()) commandChains.add(commandChain)
        else moves.forEach {
            moveChecker(board.clone(), killer, it, mutableListOf<String>().apply { addAll(commandChain); add(it) })
        }
    }

    private fun moveQueen(board: Checkerboard, checker: Checker, command: String="", commandChain: MutableList<String> = mutableListOf()) {
        val killer = if (command.isBlank()) checker else actions.killByQueen(board, command)
        val moves = findKillMovesForQueen(board, killer)
        if (moves.isEmpty() && commandChain.isNotEmpty()) commandChains.add(commandChain)
        else moves.forEach {
            moveQueen(board.clone(), killer, it, mutableListOf<String>().apply { addAll(commandChain); add(it) })
        }
    }

    private fun calculateForQueens() {
        checkerboard.getCheckers(color).filter { it.queen }.forEach { moveQueen(checkerboard, it) }
    }

    private fun getDiagonals(place: Position) = gameConstants.directions.map {
        val fields = mutableListOf<Position>()
        var checkerPosition = place
        while (true) {
            checkerPosition.moveByDirection(it)?.let {
                fields.add(it)
                checkerPosition = it
            } ?: break
        }
        fields
    }.filter { it.isNotEmpty() }
}
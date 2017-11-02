object actions {
    fun kill(board: Checkerboard, command: String): Checker {
        val (from, to) = parse(command)
        if (board.getField(from).checker!!.queen) return killByQueen(board, from, to)
        else return killByChecker(board, from, to)
    }

    fun killByChecker(board: Checkerboard, from: Position, to: Position): Checker {
        return board.move(from, to).also {
            testQueen(it)
            board.remove(Position((from.col + to.col) / 2, (from.row + to.row) / 2))
        }
    }

    fun killByQueen(board: Checkerboard, command: String): Checker {
        val (from, to) = parse(command)
        return killByQueen(board, from, to)
    }

    fun killByQueen(board: Checkerboard, from: Position, to: Position): Checker {
        var place = from
        with(board) {
            val checker = move(from, to).apply { queen = true }
            val dir: Direction
            if (from.row > to.row) {
                if (from.col > to.col) dir = Direction.SW
                else dir = Direction.SE
            } else {
                if (from.col > to.col) dir = Direction.NW
                else dir = Direction.NE
            }
            do {
                place = place.moveByDirection(dir)!!
            } while (board.getField(place).checker == null)
            remove(place)
            return checker
        }
    }

    fun testQueen(checker: Checker) {
        checker.apply {
            if (!queen && field!!.position.row == 7 - 7*color) queen = true
        }
    }

    private fun parse(command: String): Pair<Position, Position> {
        val fields = command.split(":")
        val from = Position(fields[0])
        val to = Position(fields[1])
        return Pair(from, to)
    }
}

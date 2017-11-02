class Checkerboard(board: List<List<Field>>? = null) {
    val board = board ?: (1..8).map { y ->
        (1..8).map {
            x ->
            Field((x + y) % 2, Position(gameConstants.letters[x - 1] + (9 - y)))
        }
    }

    companion object {
        fun create(): Checkerboard {
            val checkerboard = Checkerboard()
            val placeChecker = fun(to: String, color: Int) {
                val checker = Checker(color)
                checkerboard.place(checker, to)?.let {
                    checker.field = it
                }
            }
            gameConstants.letters.forEach{ s ->
                (1..3).forEach { y -> placeChecker("$s$y", Color.WHITE) }
                (6..8).forEach { y -> placeChecker("$s$y", Color.BLACK) }
            }
            return checkerboard
        }

        fun decode(code: List<Double>): Checkerboard {
            val checkerboard = Checkerboard()
            checkerboard.board.flatMap { it }.filter { it.color == 1 }.forEachIndexed { i, field ->
                when(code[i]) {
                    0.5 -> field.checker = Checker(0, field)
                    -0.5 -> field.checker = Checker(1, field)
                    1.0 -> field.checker = Checker(0, field, true)
                    -1.0 -> field.checker = Checker(1, field, true)
                }
            }
            return checkerboard
        }
    }

    fun getCheckers(color: Int) = getCheckers().filter { it.color == color }

    fun getCheckers() = board.flatMap { it }.map { it.checker }.filterNotNull()

    fun clone(): Checkerboard {
        val board = board.map {
            it.map {
                val checker = if (it.checker == null) null else {
                    Checker(it.checker!!.color).apply { id = it.checker!!.id }
                }
                val field = Field(it.color, Position(it.position.strPosition), checker)
                checker?.field = field
                field
            }
        }
        return Checkerboard(board)
    }

    fun show() {
        show(board)
    }

    fun show(board: List<List<Field>>) {
        val desk = board.map { row ->
            row.map { (color, _, checker) ->
                if (color == Color.WHITE) {
                    "   "
                } else {
                    if (checker == null) {
                        "[ ]"
                    } else {
                        if (checker.color == Color.WHITE) {
                            if (!checker.queen) "[o]"
                            else "[@]"
                        } else {
                            if (!checker.queen) "[x]"
                            else "[#]"
                        }
                    }
                }
            }.reduce { s1, s2 -> s1 + s2 }
        }.mapIndexed { y, row -> "${8-y} |$row\n" }.reduce { s1, s2 -> s1 + s2 } +
                "   ------------------------\n    a  b  c  d  e  f  g  h"
        println(desk)
    }

    fun getField(place: String) = getField(Position(place))

    fun getField(place: Position): Field {
        return board[7-place.row][place.col]
    }

    fun place(checker: Checker?, to: String): Field? {
        val field = getField(to)
        // нельзя перемещать шашку на белое или на занятое поле (удалять можно)
        if (field.color == Color.WHITE || (field.checker != null && checker != null)) return null
        field.checker?.field = null
        field.checker = checker
        checker?.field = field
        return field
    }

    fun remove(from: String) {
        place(null, from)
    }

    fun remove(from: Position) {
        remove(from.strPosition)
    }

    fun move(from: String, to: String): Checker {
        val a = getField(from)
        val b = getField(to)
        val checker = a.checker ?: throw CheckerException("Не могу переместить шашку. Ход - $from-$to")
        checker.field = b
        a.checker = null
        b.checker = checker
        return checker
    }

    fun move(from: Position, to: Position) = move(from.strPosition, to.strPosition)

    fun encodeToVector() = board.flatMap { it }.filter { it.color == 1 }.map { it.checker }.map {
        var a = 0.0
        if (it != null) {
            val checker = it.field!!.checker!!
            if (checker.color == 0) a = 1.0
            else a = -1.0
            if (checker.queen) a *= 3.0
        }
        a
    }

    fun testEncodeToVector() = board.flatMap { it }.filter { it.color == 1 }.map { it.toString() }

    fun encodeToVectorByMirror()  = board.toMutableList().map { it.reversed() }.reversed().flatMap { it }.filter { it.color != 0 }.map {
        var res = 0.0
        if (it.checker != null) {
            if (it.checker!!.color == 1) res = 0.5
            else res = -0.5
            if (it.checker!!.queen) res *= 2
        }
        res
    }
}
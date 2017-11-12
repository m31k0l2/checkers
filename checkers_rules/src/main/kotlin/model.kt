/***********************************************************************************************************************
 *
 * Модель шашечной доски, шашек
 *
 **********************************************************************************************************************/

/** Описание шашки
 * [color] = 1 - чёрный цвет, 0 - белый
 * [type] = 0 - шашка, 1 - дамка
 */
data class BoardChecker(val color: Int, var type: Int=0)

/** Описание поля доски
 * [x], [y] - координаты поля
 * [color] = 1 - чёрный цвет, 0 - белый
 * [checker] - если null, то поле пустое, иначе на поле располагается шашка
 */
data class BoardField(val x: Int, val y: Int, val color: Int, var checker: BoardChecker? = null)

/**
 * Другое задание координат
 * Автоматические преобразует координаты в запись принятую в русских шашках (a1, c1, ...) и обратно
 */
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

/**
 * Извлекает координату x из текстовой записи позиции
 */
private fun String.getX() = ('a'..'h').map { it.toString() to it.toInt() - 96 }.toMap()[substring(0, 1)]!!

/**
 * Извлекает координату y из текстовой записи позиции
 */
private fun String.getY() = substring(1, 2).toInt()

/**
 * Описание шашечной доски
 * [board] - список полей доски
 */
class CheckersBoard {
    private val board: List<BoardField>

    init {
        board = (1..8).flatMap { y -> (1..8).map { x -> BoardField(x, y, (x + y + 1) % 2 ) } }
    }

    /**
     * Получить конкретное поле по координатам
     */
    private fun get(x: Int, y: Int) = board.find { it.x == x && it.y == y }

    /**
     * Получить конкретное поле по переданной позиции
     */
    fun get(pos: BoardPosition) = get(pos.x, pos.y)

    /**
     * Получить конкретное поле через текстовое выражение
     */
    fun get(pos: String) = get(BoardPosition(pos))

    /**
     * Текстовое отображение доски
     */
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

    /**
     * Размещает шашку [checker] в поле с координатами [x], [y]
     */
    private fun place(x: Int, y: Int, checker: BoardChecker?) {
        if (checker?.type == 0 && ((y == 8 && checker.color == 0) || (y == 1 && checker.color == 1))) checker.type = 1
        get(x, y)!!.checker = checker
    }

    /**
     * Размещает шашку [checker] в поле с позицией [pos]
     */
    private fun place(pos: BoardPosition, checker: BoardChecker?) = place(pos.x, pos.y, checker)

    /**
     * Размещает шашку [checker] в поле с позицией [pos], заданной текстовым представлением
     */
    fun place(pos: String, checker: BoardChecker?) = place(BoardPosition(pos), checker)

    /**
     * Переместить содержимое поле [from] в поле [to], поля задаются текстовым представлением
     */
    fun move(from: String, to: String) {
        val checker = get(from)!!.checker
        place(from, null)
        place(to, checker)
    }

    /**
     * Обнулить содержимое поля, с координатами [x], [y]
     */
    private fun remove(x: Int, y: Int)= place(x, y, null)

    /**
     * Обнулить содержимое полей на диагонали между полями в позиции [from] и в позиции [to] включительно
     */
    private fun remove(from: BoardPosition, to: BoardPosition) {
        val x1 = Math.min(from.x, to.x)
        val x2 = Math.max(from.x, to.x)
        val y1 = Math.min(from.y, to.y)
        val y2 = Math.max(from.y, to.y)
        (x1..x2).forEach { x -> (y1..y2).forEach { y -> remove(x, y) } }
    }

    /**
     * Обнулить содержимое полей на диагонали между полями в позиции [from] и в позиции [to] включительно.
     * Позиции заданы текстовыми представлениями
     */
    fun remove(from: String, to: String) = remove(BoardPosition(from), BoardPosition(to))

    /**
     * Получить список позиций шашек на доске цвета [color]
     */
    fun getCheckers(color: Int) = board.filter {
        it.checker != null && it.checker!!.color == color }.map { BoardPosition(it.x, it.y) }

    /**
     * Убрать все шашки с доски. Достигается обнулением полей
     */
    fun clear() {
        board.forEach { it.checker = null }
    }
}
/***********************************************************************************************************************
 *
 * Модель шашечной доски, шашек
 *
 **********************************************************************************************************************/

/** Описание шашки
 * [color] = 1 - чёрный цвет, 0 - белый
 * [type] = 0 - шашка, 1 - дамка
 */
data class Checker(val color: Int, var type: Int=0)

/** Описание поля доски
 * [x], [y] - координаты поля
 * [color] = 1 - чёрный цвет, 0 - белый
 * [checker] - если null, то поле пустое, иначе на поле располагается шашка
 */
data class Field(val x: Int, val y: Int, val color: Int, var checker: Checker? = null)

/**
 * Другое задание координат
 * Автоматические преобразует координаты в запись принятую в русских шашках (a1, c1, ...) и обратно
 */
class Position(val x: Int, val y: Int) {
    constructor(s: String) : this(s.getX(), s.getY())

    override fun toString(): String {
        val ch = ('a' + x - 1).toString()
        return "$ch$y"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Position
        return x == other.x && y == other.y
    }

    override fun hashCode() = 31 * x + y

    /** Возвращает позицию с приращением dx, dy, если это не возможно, то вернёт null **/
    fun next(dx: Int, dy: Int): Position? {
        val x = x + dx
        val y = y + dy
        if (y < 1 || y > 8 || x < 1 || x > 8) return null
        return Position(x, y)
    }
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
class Checkerboard {
    val board: List<Field>

    init {
        board = (1..8).flatMap { y -> (1..8).map { x -> Field(x, y, (x + y + 1) % 2 ) } }
    }

    /**
     * Получить конкретное поле по координатам
     */
    private fun get(x: Int, y: Int) = board.find { it.x == x && it.y == y }

    /**
     * Получить конкретное поле по переданной позиции
     */
    fun get(pos: Position) = get(pos.x, pos.y)

    /**
     * Получить конкретное поле через текстовое выражение
     */
    fun get(pos: String) = get(Position(pos))

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
    private fun place(x: Int, y: Int, checker: Checker?) {
        if (checker?.type == 0 && ((y == 8 && checker.color == 0) || (y == 1 && checker.color == 1))) checker.type = 1
        get(x, y)!!.checker = checker
    }

    /**
     * Размещает шашку [checker] в поле с позицией [pos]
     */
    private fun place(pos: Position, checker: Checker?) = place(pos.x, pos.y, checker)

    /**
     * Размещает шашку [checker] в поле с позицией [pos], заданной текстовым представлением
     */
    fun place(pos: String, checker: Checker?) = place(Position(pos), checker)

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
    private fun remove(from: Position, to: Position) {
        val dx = if (to.x - from.x < 0) -1 else 1
        val dy = if (to.y - from.y < 0) -1 else 1
        (0..Math.abs(to.x - from.x)).forEach { remove(from.x + it * dx, from.y + it * dy) }
    }

    /**
     * Обнулить содержимое полей на диагонали между полями в позиции [from] и в позиции [to] включительно.
     * Позиции заданы текстовыми представлениями
     */
    fun remove(from: String, to: String) = remove(Position(from), Position(to))

    /**
     * Получить список позиций шашек на доске цвета [color]
     */
    fun getCheckers(color: Int) = board.filter {
        it.checker != null && it.checker!!.color == color }.map { Position(it.x, it.y) }

    /**
     * Убрать все шашки с доски. Достигается обнулением полей
     */
    private fun clear() {
        board.forEach { it.checker = null }
    }

    /** Клонирование доски **/
    fun clone(): Checkerboard {
        val clone = Checkerboard()
        for (field in board) {
            val checker = field.checker
            clone.get(field.x, field.y)?.checker = if (checker == null) null else Checker(checker.color, checker.type)
        }
        return clone
    }

    /** Преобразует позиции на доске в вектор действительных чисел
     * Каждое число указывает на ценность шашки, простые шашки стоят 1, а дамки 3
     * Цвет фигур задаётся знаком
     */
    fun encodeToVector() = board.filter { it.color == 1 }.map {
        val checker = it.checker ?: return@map 0.0
        if (checker.color == 0 && checker.type == 0) return@map 1.0
        if (checker.color == 0 && checker.type == 1) return@map 3.0
        if (checker.color == 1 && checker.type == 0) -1.0 else -3.0
    }

    /**
     * Инициализация шашек на доске. Для уточнения дамок воспользоваться функцией queen
     */
    fun init(whiteCheckers: List<String>, blackCheckers: List<String>) {
        clear()
        whiteCheckers.forEach { place(it, Checker(0)) }
        blackCheckers.forEach { place(it, Checker(1)) }
    }
}
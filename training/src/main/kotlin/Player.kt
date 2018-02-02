import java.util.*
import kotlin.streams.toList

class Player(val nw: Network, private val predicateMoves: Int = 2,
             val error: Double = 0.0, private val debug: Boolean = false) {
    /**
     * Выбор ИИ наилучшего хода
     * [checkerboard] - доска на момент хода
     * [color] - цвет фигур ИИ
     * [steps] - набор возможных ходов
     *
     * Оценка каждого хода производится в паралельных потоках
     * Оценка осуществляется путём проигрывания ходов вперёд в количестве [predicateMoves]
     * В итоге получается список пар {ход - счёт}
     * Выбирается ход с максимальным счётом
     * Если [debug] == true, то для отладки показывает, как думает ИИ и печатает выбранный им ход
     */
    fun selectMove(checkerboard: Checkerboard, color: Int, steps: List<String>): String {
        if (steps.size == 1) return steps.first()
        val pairs = stepsToPairs(steps, checkerboard)
        if (predicateMoves == 0) {
            return if (color == 0) pairs.sortedBy { it.second }.last().first
            else pairs.sortedBy { it.second }.first().first
        }
        val goodPairs = filterGoodSteps(pairs, color)
        val list = goodPairs.parallelStream().map {
            it to play(checkerboard, color, predicateMoves, it.first) }.toList()
        val max = list.maxBy { it.second }?.second
        val l = list.filter { it.second == max }.map { it.first }
        val step = selectBestStep(l, color, debug)
        if (debug) {
            println("все ходы: $steps")
            println("ожидания: $pairs")
            println("лучшие ожидания: $goodPairs")
            println("ходы с предполагаемой выгодой: $list")
            println("лучший ход: $step")
        }
        return step
    }

    /**
     * Проигрывание ИИ партии с позиций доски [checkerboard]
     * [initColor] - цвет ИИ
     * [count] - количество проигрываемых ИИ ходов
     * [initStep] - рассматриваемый ход
     *
     * ИИ играет сам с собой за чёрных и белых
     * Начинает с заданного хода
     * По окончанию ходов или по результатам партии (если до выделенного количество ходов игра окончена)
     * происходит подсчёт очков
     * За выйгранную партию начисляется 100 очков, за проигрыш - минус 100
     * Чтобы выбрать, который приблежает победу или оттягивает поражение, из 100 вычтим количество ходов, затраченных в игре
     * Если результат не определен, очки начисляются за оставшееся количество шашек
     * стоимость шашки - 1 очко, дамки - 3
     * Результат определяется как разность очков между белыми и чёрными
     */
    private fun play(checkerboard: Checkerboard, initColor: Int, count: Int, initStep: String): Int {
        val game = GameController(checkerboard.clone())
        game.currentColor = initColor
        lateinit var steps: List<Pair<String, Double>>
        for (i in 0 until count * 2) {
            val step = if (i == 0) initStep else {
                steps = filterGoodSteps(stepsToPairs(game.nextMoves(), game.checkerboard), game.currentColor)
                if (steps.isEmpty()) {
                    return if (game.currentColor != initColor) 100 - i
                    else -100 + i
                }
                selectBestStep(steps, game.currentColor)
            }
            game.go(step)
            game.currentColor = 1 - game.currentColor
        }
        val whiteCount = game.checkerboard.encodeToVector().filter { it > 0 }.count()
        val blackCount = game.checkerboard.encodeToVector().filter { it < 0 }.count()
        return if (initColor == 0) whiteCount - blackCount
        else blackCount - whiteCount
    }

    /**
     * Выбор ИИ наилучшего хода в процессе оценки,
     * если выберать неизчего возвращаем результат
     *
     * Алгоритм
     * Для каждого хода из заданного списка
     * Положение шашек на доске кодируется в вектор действительных чисел.
     * Размер вектора соответсвует игровым полям доски, т.е. 32
     * Этот вектор дальше преобразуется при помощи инкодера в вектор с размерностью 91.
     * Инкодер составляет вектор из фрагментов доски по аналогии с принципом работы свёрточных сетей.
     * Это позволяет нейронной сети, как бы рассматривать доску с разных точек зрения, а нейроны её первого слоя,
     * тогда играют роль детектора фич
     * При помощи мультиактивации, каждый вектор массива сопоставляется с одним нейроном сети первого слоя
     * Каждый нейрон выступает своего рода рецептором, который реагирует на ситуацию на доске в общем или какойто ее части
     * Дальше нейронная сеть вычисляет число от -1 до 1, где 1 соответствуют лучшему с её точки зрения ходу, а -1 - худшему
     *
     * Чтобы нейронная сеть не зависила от цвета фигур за которые она играет, оценка идёт со стороны белых
     * Если нейронная сеть играет за чёрных, то выбирается ход наиболее худший для белых (т.е. минимальное значение)
     * Если за белых, то соответственно максимальное значение
     **/
    private fun selectBestStep(steps: List<Pair<String, Double>>, color: Int, debug: Boolean = false): String {
        if (steps.isEmpty()) return ""
        if (steps.size == 1) return steps.first().first
        val random = Random()
        val list = steps.map {
            it.first to it.second * if (error > 0) (1 + error/100 * (1 - 2*random.nextDouble())) else 1.0
        } // закладываем ошибку +/- 1%
        if (debug) {
            list.forEach { println(it) }
        }
        val step = (if (color == 0) {
            list.maxBy { it -> it.second }!!
        } else list.minBy { it.second }!!)
        if (debug) {
            println(step)
        }
        return step.first
    }

    private fun stepsToPairs(steps: List<String>, checkerboard: Checkerboard) = steps.parallelStream().map {
        val game = GameController(checkerboard.clone())
        game.go(it)
        val x = game.checkerboard.encodeToVector()
        val o = nw.multiActivate(InputEncoder().encode(x))
        it to o.first()
    }.toList()

    private fun filterGoodSteps(steps: List<Pair<String, Double>>, color: Int): List<Pair<String, Double>> {
        if (steps.size < 3) return steps
        val median = steps.map { it.second }.median()
        return if (color == 0) {
            steps.filter { it.second >= median }
        } else {
            steps.filter { it.second <= median }
        }
    }
}

private fun List<Double>.median() = sorted()[size/2]
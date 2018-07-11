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
     * Оценка каждого хода производится в параллельных потоках
     * Оценка осуществляется путём проигрывания ходов вперёд в количестве [predicateMoves]
     * В итоге получается список пар {ход - счёт}
     * Выбирается ход с максимальным счётом
     * Если [debug] == true, то для отладки показывает, как думает ИИ и печатает выбранный им ход
     */
    fun selectMove(checkerboard: Checkerboard, color: Int, steps: List<String>): String {
        TODO()
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
     * За выигранную партию начисляется 100 очков, за проигрыш - минус 100
     * Чтобы выбрать, который приближает победу или оттягивает поражение, из 100 вычтем количество ходов, затраченных в игре
     * Если результат не определен, очки начисляются за оставшееся количество шашек
     * стоимость шашки - 1 очко, дамки - 3
     * Результат определяется как разность очков между белыми и чёрными
     */
    private fun play(checkerboard: Checkerboard, initColor: Int, count: Int, initStep: String): Int {
        TODO()
    }

    /**
     * Выбор ИИ наилучшего хода в процессе оценки,
     * если выбирать неизчего возвращаем результат
     *
     * Алгоритм
     * Для каждого хода из заданного списка
     * Положение шашек на доске кодируется в вектор действительных чисел.
     * Нейронную сеть активируем этим вектором
     * Дальше нейронная сеть вычисляет число от -1 до 1, где 1 соответствуют лучшему с её точки зрения ходу, а -1 - худшему     *
     * Чтобы нейронная сеть не зависела от цвета фигур за которые она играет, оценка идёт со стороны белых
     * Если нейронная сеть играет за чёрных, то выбирается ход наиболее худший для белых (т.е. минимальное значение)
     * Если за белых, то соответственно максимальное значение
     * Для ускорения выбора хода, отсеем по медиане худшие значения, а на оставшиеся проиграем игру на два хода вперёд
     * Сеть конечно определяет ход противника, исходя из своих значений весов
     **/
    private fun selectBestStep(steps: List<Pair<String, Double>>, color: Int, debug: Boolean = false): String {
        if (steps.isEmpty()) return ""
        if (steps.size == 1) return steps.first().first
        val random = Random()
        val list = steps.map {
            it.first to it.second * if (error > 0) (1 + error/100 * (1 - 2*random.nextDouble())) else 1.0
        } // закладываем ошибку исключить повтор ходов
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
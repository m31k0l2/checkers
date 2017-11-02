/**
 * Решение задачи XOR
 * [testData] - тестовые данные для задачи XOR, набор данных генерируем в виде:
 * [-1; -1] => +1
 * [-1; +1] => -1
 * [+1; -1] => -1
 * [+1; +1] => +1,
 * где -1 = ЛОЖЬ, +1 = ИСТИНА
 */
class EvolutionXOR(populationSize: Int, scale: Int): AbstractEvolution(populationSize, scale) {
    private val testData: Map<List<Double>, Double> = generateTestData()

    private fun generateTestData(): Map<List<Double>, Double> {
        val answer = listOf(1, -1, -1, 1).map { it.toDouble() }
        return listOf(-1, -1, -1, 1, 1, -1, 1, 1).map { it.toDouble() }
                .batch(2).mapIndexed { i, xi -> xi to answer[i] }.toMap()
    }

    override fun createNet() = Network(2, 1)

    /** соревнование игрока (сети) "a" с игроком (сетью) "b"
     * если пара игроков образована из одного игрока возвращаем 0 очков
     * в случае победы игрока "a" возвращаем 1 очко
     * в случае ничьи 0 очков
     * в случае проигрыша игрока "d" возвращем -2 очка
     * Выбор очков обосновывается следующими соображениями: наказание должно быть в два раза сильнее чем поощерение.
     * Победа или проигрыш определяется величиной ошибки допущенной при решении задачи XOR
     */
    override fun play(a: Network, b: Network): Int {
        if (a == b) return 0
        val costA = cost(a)
        val costB = cost(b)
        if (costA < costB) return 1
        if (costA == costB) return 0
        return -2
    }

    /**
     * Определение ошибки нейронной сети при решении задачи XOR.
     * Ошибка суммируется по всем тестовым данным.
     *
     */
    private fun cost(nw: Network)= testData.map { (x, d) ->
        // x - вводные данные для активации сети
        // d - желаемый результат
        val o = nw.activate(x) // выход сети
        val e = (d - o[0])
        e*e
    }.sum()

    /** Проверка правильности выполнения задачи */
    fun test(nw: Network) = testData.keys.forEach {
        println("$it -> ${nw.activate(it)}")
    }
}

fun main(args: Array<String>) {
    val evolution = EvolutionXOR(30, 10)
    val nw = evolution.evolute(500).nw
    evolution.test(nw)
}
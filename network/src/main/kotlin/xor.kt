import kotlin.system.measureTimeMillis

/**
 * Решение задачи XOR
 * [testData] - тестовые данные для задачи XOR, набор данных генерируем в виде:
 * [-1; -1] => -1
 * [-1; +1] => +1
 * [+1; -1] => +1
 * [+1; +1] => -1,
 * где -1 = ЛОЖЬ, +1 = ИСТИНА
 */
class EvolutionXOR(populationSize: Int, scale: Int, mutantRate: Double=0.1): AbstractEvolution(populationSize, scale, mutantRate) {
    private val testData: Map<List<Double>, Double> = generateTestData()
    lateinit var population: List<Individual>

    private fun generateTestData(): Map<List<Double>, Double> {
        val answer = listOf(-1, 1, 1, -1).map { it.toDouble() }
        return listOf(-1, -1, -1, 1, 1, -1, 1, 1).map { it.toDouble() }
                .chunked(2).mapIndexed { i, xi -> xi to answer[i] }.toMap()
    }

    override fun createNet() = Network(16, 8, 4, 2, 1)

    /** соревнование игрока (сети) "a" с игроком (сетью) "b"
     * если пара игроков образована из одного игрока возвращаем 0 очков
     * в случае победы игрока "a" возвращаем 1 очко
     * в случае ничьи 0 очков
     * в случае проигрыша игрока "d" возвращем -2 очка
     * Выбор очков обосновывается следующими соображениями: наказание должно быть в два раза сильнее чем поощерение.
     * Победа или проигрыш определяется величиной ошибки допущенной при решении задачи XOR
     */
    override fun play(a: Network, b: Network) = if (cost(a) < cost(b)) 1 else -2

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

    override fun evoluteEpoch(initPopulation: List<Individual>): List<Individual> {
        population = super.evoluteEpoch(initPopulation)
        return population
    }
}

fun main(args: Array<String>) {
    val time = measureTimeMillis {
        val evolution = EvolutionXOR(20, 3, 0.01)
        val nw = evolution.evolute(200).nw
        evolution.test(nw)
        evolution.population.forEachIndexed { index, individual ->
            NetworkIO().save(individual.nw, "nets/xor/save$index.net")
        }
    }
    println(time)
}
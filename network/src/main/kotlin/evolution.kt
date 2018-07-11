import java.io.File
import java.util.*
import java.util.stream.Collectors
import kotlin.math.max

/**
 * Модель особи.
 * [nw] - нейронная сеть, [rate] - рейтинг выживаемости
 */
data class Individual(val nw: Network, var rate: Int)

/**
 * [populationSize] - размер популяции
 * [scale] - предельное значение гена (веса сети). Вес выбирается из диапазона [-scale; +scale]
 * [crossoverRate] - параметр регулирующий передачу генов от родителей при скрещивании
 * 0,5 - половина от отца, половина от матери
 * 0,3 - 30 % от отца, 70 % от матери
 * 1,0 - все гены от матери
 * [mutantRate] - вероятность мутации генов, не должно быть сильно большим значением, основную роль в эволюции
 * должно играть скрещивание
 */
abstract class NetEvolution(
        var maxMutateRate: Double=0.2,
        private val scale: Int=1
) {
    private val random = Random()
    var mutantRate = 1.0
    var genMutateRate = 0.05
    lateinit var mutantStrategy: (epoch: Int, epochSize: Int) -> Double
    var trainLayers = emptyList<Int>()
    private var mutateRate = 0.1
    private var alpha: Double = 1.0

    init {
        if (File("nets/").mkdir()) {
            println("Создаю каталог nets/")
        }
    }

    /**
     * Запуск эволюции.
     * [epochSize] - количество поколений которые участвуют в эволюции (эпох).
     * Создаём популяцию размером populationSize
     * Выполняем эволюцию популяции от эпохи к эпохе
     */

    fun evolute(epochSize: Int, initPopulation: Set<Network>, alpha: Double): Set<Network> {
        if (trainLayers.isEmpty()) trainLayers = (0 until initPopulation.first().layers.size).toList()
        this.alpha = alpha
        var population = initPopulation
        mutateRate = max((Random().nextDouble()*maxMutateRate*100).toInt()/100.0, 0.005)
        (0 until epochSize).forEach { curEpoch ->
            println("эпоха $curEpoch")
            val start = System.nanoTime()
            mutantRate = mutantStrategy(curEpoch, epochSize)
            population = evoluteEpoch(population)
            val fin = System.nanoTime()
            println("мутация ${(mutantRate * 1000).toInt() / 10.0} % / $mutateRate")
            println("Время: ${(fin - start) / 1_000_000} мс\n")
        }
        return population
    }

    /**
     * эволюция популяции за одну эпоху
     * среди популяции проводим соревнование
     * По итогам генерируется новое поколение для следующей эпохи
     * Условия размножения (mutantRate и crossoverRate) изменяются от эпохи к эпохе
     **/

    private fun evoluteEpoch(initPopulation: Set<Network>): Set<Network> {
        val population = tournament(initPopulation)
        println(population)
        println("winner rate: " + population.first().rate)
        return nextGeneration(population)
    }

    /**
     * Выводим новое поколение популяции.
     * Для начала убиваем половину самых слабых особей. Восстановление популяции произойдёт за счёт размножения
     * Потом выживших случайным образом разбиваем на пары.
     * Скрещивание будет происходить беспорядочно, т.е. не моногамно, каждая особь может быть в паре с любой
     * другой особью несколько раз, а количество пар будет равно количеству популяции
     * Пары скрещиваем и получаем потомство. Скрещивание выполняется многопоточно.
     * Одна пара рождает одного ребёнка, таким образом получаем, что в итоге количество скрещивающихся особей будет
     * соответствовать рождённым особям.
     * Из числа родителей и потомства составляем новое поколение популяции
     * Для исключения вырождения популяции можем удваивать на время мутацию. Под вырождением понимаем критическое
     * совпадение генов у всех особей популяции. Это означает, что созданный шаблон при исходных наборах гена оптимален.
     * Этот оптимальный шаблон сохраним, для дальнейшего сравнения и использования
     */
    private fun nextGeneration(survivors: List<Individual>): Set<Network> {
        val parents = selection(survivors)
        var offspring = createChildren(parents)
        offspring = offspring.map {
            if (random.nextDouble() < mutantRate) mutate(it) else it
        }
        return survivors.map { it.nw }.union(offspring)
    }

    private fun createChildren(parents: List<Pair<Individual, Individual>>) = parents.parallelStream().map { cross(it) }.collect(Collectors.toList())!!

    /**
     * Разбиваем популяцию по парам для их участия в скрещивании
     */
    private fun selection(population: List<Individual>): List<Pair<Individual, Individual>> {
        val size = population.size
        val s = size*(size + 1.0)
        var rangs = population.asReversed().mapIndexed { index, individual ->
            individual to 2*(index+1)/s
        }
        var rangCounter = 0.0
        rangs = rangs.map {
            rangCounter += it.second
            it.first to rangCounter
        }
        return (0 until population.size).map {
            val p1 = random.nextDouble()
            val p2 = random.nextDouble()
            val select = { p: Double -> rangs.filter { it.second > p }.random().first }
            select(p1) to select(p2)
        }
    }

    private fun getCrossoverRate(parents: Pair<Individual, Individual>) = if (parents.first.rate < parents.second.rate) {
        0.5 + 0.2*random.nextDouble()
    } else {
        0.5 - 0.2*random.nextDouble()
    }

    private fun cross(parents: Pair<Individual, Individual>): Network {
        val crossoverRate = getCrossoverRate(parents)
        val nw = parents.first.nw.clone()
        trainLayers.forEach { l ->
            val layer = nw.layers[l]
            val neurons = layer.neurons
            for (i in 0 until neurons.size) {
                if (random.nextDouble() > crossoverRate) {
                    neurons[i] = parents.second.nw.layers[l].neurons[i]
                }
            }
        }
        return nw
    }

    private fun mutate(net: Network): Network {
        val nw = net.clone()
        for (l in trainLayers) {
            val layer = nw.layers[l]
            for (neuron in layer.neurons) {
                if (random.nextDouble() < mutateRate) {
                    val weights = neuron.weights
                    val n = Random().nextInt(max(1.0, genMutateRate * weights.size).toInt())
                    for (i in (0..n)) {
                        weights[Random().nextInt(weights.size)] = (1 - 2 * random.nextDouble()) * scale
                    }
                }
            }
        }
        return nw
    }
}

private fun <E> List<E>.random() = get(Random().nextInt(size))

class CheckerEvolution(maxMutateRate: Double=0.1, scale: Int=1) : NetEvolution(maxMutateRate, scale)

fun main(args: Array<String>) {
    //buildNetwork(Random().nextInt(4)+2, Random().nextInt(4)+2, Random().nextInt(4)+2, Random().nextInt(20)+4, 1) }.toSet()
    val evolution = CheckerEvolution().apply {
        mutantStrategy = {epoch, epochSize -> if (epoch < 50) (epochSize-epoch*1.0)/epochSize else 0.1 } }
//    evolution.evolute(15, List(32) { buildNetwork(3, 5, 7, 40, 1) }.toSet(), 15.0)
    evolution.evolute(1000, List(31) { buildNetwork(3, 5, 7, 40, 1) }.toSet().union(listOf(CNetwork().load("nets/win.net")!!)), 15.0)
}
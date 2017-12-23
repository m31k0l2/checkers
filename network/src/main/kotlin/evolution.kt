import java.util.*
import java.util.stream.Collectors

/** Расширение класса List. Позволяет разбить список типа [a, b, c, d, e..] на батчи типа [[a, b, c], [d, e, f] ...]
 * [batchSize] - размер батча
 */
fun <E> List<E>.batch(batchSize: Int) = (0 until size/ batchSize).map {
    subList(it* batchSize, (it+1)* batchSize) }

/**
 * Модель особи.
 * [nw] - нейронная сеть, [rate] - рэйтинг выживаемости
 */
data class Individual(val nw: Network, var rate: Int=0)

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
abstract class AbstractEvolution(
        private val populationSize: Int,
        private val scale: Int
) {
    private var crossoverRate = 0.5
    private var mutantRate = 0.1
    private val random = Random()

    /**
     * Запуск эволюции.
     * [epochSize] - количество поколений которые участвуют в эволюции (эпох).
     * Создаём популяцию размером populationSize
     * Выполняем эволюцию популяци от эпохи к эпохе
     */
    fun evolute(epochSize: Int): Individual {
        var population = generatePopulation(populationSize)
        (0 until epochSize).forEach { population = evoluteEpoch(population) }
        return population.first()
    }

    /**
     * эволюция популяции за одну эпоху
     * среди популяции проводим соревнование
     * По итогам генерируется новое поколение для следующей эпохи
     * Условия размножения (mutantRate и crossoverRate) изменяются от эпохи к эпахе
     **/
    open fun evoluteEpoch(initPopulation: List<Individual>): List<Individual> {
        var population = competition(initPopulation)
        population = nextGeneration(population) // генерируем следующее поколение особей
        // случайным образом регулируем эволюцию для следующего поколения
        mutantRate = random.nextDouble() * 0.5 // в пределах [0; 0.5]
        crossoverRate = random.nextDouble() // -//- [0; 1.0]
        return population
    }

    /**
     * Создаёт популяцию особей заданного размера [size]
     */
    open fun generatePopulation(size: Int) = (0 until size).map { createIndividual() }

    private fun createIndividual() = Individual(createNet())

    /**
     * Задаётся топология сети
     */
    abstract fun createNet(): Network

    /**
     * Проводит соревнование внутри популяции. На выходе вычисляет поколение
     * Каждая особь соревнуется со случайно отобранными в количестве [playerCount] особями-конкурентами.
     * По итогам соревнования для каждой особи определяется величина score.
     * Score - сумма очков полученных по итогам игры двух особей (эта же величина характеризует выживаемость особи).
     * Выходом функции является популяция, в которой каждая особь отсортирована в порядке своей выживаемости
     */
    private fun competition(population: List<Individual>, playerCount: Int=5) = population.map {
        (player1, _) ->
        // score - величина, характеризующее приспособленность особей
        val score = (1..playerCount).map {
            population[random.nextInt(populationSize)].nw }.parallelStream().map {
            player2 -> play(player1, player2) }.mapToInt { it }.sum()
        Individual(player1, score)
    }.sortedBy { it.rate }.reversed()

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
     */
    open fun nextGeneration(population: List<Individual>): List<Individual> {
        val survivors = population.take(populationSize / 2)
        val parents = selection(survivors)
        val children = parents.parallelStream().map { cross(it) }.collect(Collectors.toList())
        return survivors.union(children).toList()
    }

    /**
     * Разбиваем популяцию по парам для их участия в скрещивании
     */
    private fun selection(players: List<Individual>) = (1..populationSize)
            .map { players[random.nextInt(players.size)] }
            .batch(2).map { it[0] to it[1] }

    /**
     * Выполняем скрещивание.
     * Гены потомка получаются либо путём мутации, либо путём скрещивания (определяется случайностью)
     * Если гены формируются мутацией, то значение гена выбирается случайно в диапазоне [-scale; scale]
     * Если гены формируются скрещиванием, то ген наследуется случайно от одного из родителей
     * Шанс передачи гена от родителя определяется параметром crossoverRate
     * Гены нейронной сети - это веса её нейронов
     */
    private fun cross(pair: Pair<Individual, Individual>): Individual {
        val firstParent = pair.first.nw
        val secondParent = pair.second.nw
        val firstParentGens = extractWeights(firstParent)
        val secondParentGens = extractWeights(secondParent)
        val childGens = firstParentGens.mapIndexed { l, layer ->
            layer.mapIndexed { n, neuron -> neuron.mapIndexed { w, gen ->
                if (random.nextDouble() < mutantRate) {
                    (2*random.nextDouble()-1)*scale
                } else gen.takeIf { random.nextDouble() < crossoverRate } ?: secondParentGens[l][n][w] }
            }
        }
        return Individual(generateNet(childGens))
    }

    /**
     * Создаёт нейронную сеть на основе списка весов, упакованных следующим образом:
     * [ уровень_слоя [ уровень_нейрона [ уровень_веса ] ]
     * Проходим по каждому уровню и заполняем сеть
     */
    private fun generateNet(layerWeights: List<List<List<Double>>>): Network {
        val nw = createNet()
        layerWeights.forEachIndexed { layerPosition, neuronsWeights ->
            val layer = nw.layers[layerPosition]
            layer.neurons.forEachIndexed { index, neuron ->
                neuron.weights = neuronsWeights[index].toMutableList()
            }
        }
        return nw
    }

    /** Извлекаем веса нейронной сети и упаковываем их специальным образом */
    private fun extractWeights(nw: Network) = nw.layers.map { it.neurons.map { it.weights.toList() } }

    /** соревнование двух сетей, на выходе начисляются очки */
    abstract fun play(a: Network, b: Network): Int
}
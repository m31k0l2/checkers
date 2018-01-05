import java.io.File
import java.util.*

/**
 * Обучение нейронной сети при помощи эволюционного алгоритма игре Шашки
 * [layersCapacity] - структура сети в виде списка, содержащего размеры скрытых слоёв
 * [scale] - предельное значение гена (веса сети). Вес выбирается из диапазона [-scale;  scale]
 * [maxSteps] - максимальное количество ходов до прекращения игры
 * [predicateMoves] - количество ходов вперёд на которые бот будет анализировать игру
 */
class EvolutionCheckers(populationSize: Int,
                        private val layersCapacity: List<Int>,
                        scale: Int,
                        private val maxSteps: Int = 50,
                        private val predicateMoves: Int = 4) : AbstractEvolution(populationSize, scale) {
    private var curEpoch = 0
    private val savePerEpoch = 5
    private lateinit var population: List<Individual>
    val folder = "nets/" + layersCapacity.map { it.toString() }.reduce { acc, s -> "$acc-$s" }

    init {
        if (File("nets/").mkdir()) {
            println("Создаю каталог nets/")
        }
        if (File(folder).mkdir()) {
            println("Создаю каталог $folder")
        }
    }

    override fun play(a: Network, b: Network): Int {
        val player1 = Player(a, predicateMoves)
        val player2 = Player(b, predicateMoves)
        return if (Random().nextBoolean()) {
            1.takeIf { play(player1, player2) == 0 } ?: -2
        } else {
            1.takeIf { play(player2, player1) == 1 } ?: -2
        }
    }

    override fun createNet(): Network {
        val list = listOf(listOf(91), layersCapacity, listOf(1)).flatMap { it }
        return Network(*list.toIntArray())
    }

    // Переопределяем с целью иметь возможность записи и загрузки
    override fun generatePopulation(size: Int): List<Individual> {
        if (!File("$folder/save0.net").exists()) return super.generatePopulation(size)
        val io = NetworkIO()
        return (0 until size).map {
            try {
                Individual(io.load("$folder/save$it.net")!!)
            } catch (e: Exception) {
                Individual(createNet())
            }
        }
    }

    private fun play(player1: Player, player2: Player): Int {
        val game = GameController()
        var moves: List<String>
        var curStep = 0
        while (++curStep < maxSteps) {
            moves = game.nextMoves()
            if (moves.isEmpty()) {
                return game.currentColor
            }
            val player = player1.takeIf { game.currentColor == 0 } ?: player2
            game.go(player.selectMove(game.checkerboard, game.currentColor, moves))
            game.currentColor = 1 - game.currentColor
        }
        val white = game.checkerboard.encodeToVector().filter { it > 0 }.sum()
        val black = game.checkerboard.encodeToVector().filter { it < 0 }.sum()
        return if (white > Math.abs(black)) 0 else 1
    }

    // переопределяем, чтобы контролировать процесс обучения
    override fun evoluteEpoch(initPopulation: List<Individual>): List<Individual> {
        println("эпоха ${++curEpoch}")
        val start = System.nanoTime()
        population = super.evoluteEpoch(initPopulation)
        if (curEpoch % savePerEpoch == 0) saveNets()
        println(population.map { it.nw.id to it.rate }.subList(0, population.size / 2))
        val fin = System.nanoTime()
        println("Время: ${(fin-start)/1_000_000} мс\n")
        return population
    }

    /** выполняет сохранение сетей на диск **/
    fun saveNets() {
        println("saving to $folder...")
        with(NetworkIO()) {
            population.forEachIndexed { i, (nw, _) -> save(nw, "$folder/save$i.net") }
        }
    }
}

fun buildNameForNet(layersCapacity: List<Int>) =
        layersCapacity.map { it.toString() }.reduce { acc, s -> "$acc-$s" } + ".net"

fun teachNet(layersCapacity: List<Int>, populationSize: Int,
             epochSize: Int, testNet: String? = null, onlyTestNet: Boolean = false) {
    with(EvolutionCheckers(populationSize, layersCapacity, 10, 50, 2)) {
        val nw = evolute(epochSize, testNet, onlyTestNet).nw
        NetworkIO().save(nw, buildNameForNet(layersCapacity))
    }
}

fun main(args: Array<String>) {
    val populationSize = 12
    val epochSize = 10
    var testNet: String?
    var totalEpoch = 5
    val list = listOf(
            listOf(40, 20),
            listOf(50, 30, 10),
            listOf(60, 40, 30, 20)
    )
    list.forEach {
        teachNet(it, populationSize, 5)
    }
    while (true) {
        list.forEach { layersCapacity ->
            testNet = testStructure(list)
            teachNet(layersCapacity, populationSize, epochSize, testNet?.takeIf { it != buildNameForNet(layersCapacity) })
        }
        totalEpoch += 5
        println("Всего эпох: $totalEpoch")
    }
}
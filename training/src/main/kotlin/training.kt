import java.io.File
import java.util.*

/**
 * Обучение нейронной сети при помощи эволюционного алгоритма игре Шашки
 * [layersCapacity] - структура сети в виде списка, содержащего размеры скрытых слоёв
 * [scale] - предельное значение гена (веса сети). Вес выбирается из диапазона [-scale;  scale]
 * [maxSteps] - максимальное количество ходов до прекращения игры
 * [predicateMoves] - количество ходов вперёд на которые бот будет анализировать игру, выставляется в 0
 */
//class EvolutionCheckers(populationSize: Int,
//                        private val layersCapacity: List<Int>,
//                        scale: Int,
//                        private val maxSteps: Int = 50,
//                        private val predicateMoves: Int = 2,
//                        mutantRate: Double = 0.1,
//                        dir: String = "nets/",
//                        private val savePerEpoch: Int = 10
//                        ) : AbstractEvolution(populationSize, scale, mutantRate) {
//    private var curEpoch = 0
//    private lateinit var population: List<Individual>
//    val folder = dir + layersCapacity.map { it.toString() }.reduce { acc, s -> "$acc-$s" }
//
//    init {
//        if (File("nets/").mkdir()) {
//            println("Создаю каталог nets/")
//        }
//        if (File(folder).mkdir()) {
//            println("Создаю каталог $folder")
//        }
//    }
//
//    override fun play(a: Network, b: Network): Int {
//        val player1 = Player(a, predicateMoves)
//        val player2 = Player(b, predicateMoves)
//        return 1.takeIf { play(player1, player2) == 0 } ?: -2
//    }
//
//    override fun createNet(): Network {
//        val list = listOf(listOf(3, 20), layersCapacity, listOf(1)).flatMap { it }
//        return Network(*list.toIntArray())
//    }
//
//    // Переопределяем с целью иметь возможность записи и загрузки
//    override fun generatePopulation(size: Int): List<Individual> {
//        if (!File("$folder/save0.net").exists()) return super.generatePopulation(size)
//        val io = NetworkIO()
//        return (0 until size).map {
//            try {
//                Individual(io.load("$folder/save$it.net")!!)
//            } catch (e: Exception) {
//                Individual(createNet())
//            }
//        }
//    }
//
//    private fun play(player1: Player, player2: Player): Int {
//        val game = GameController()
//        var moves: List<String>
//        var curStep = 0
//        while (++curStep < maxSteps) {
//            moves = game.nextMoves()
//            if (moves.isEmpty()) {
//                return game.currentColor
//            }
//            val player = player1.takeIf { game.currentColor == 0 } ?: player2
//            game.go(player.selectMove(game.checkerboard, game.currentColor, moves))
//            game.currentColor = 1 - game.currentColor
//        }
//        val white = game.checkerboard.encodeToVector().filter { it > 0 }.sum()
//        val black = game.checkerboard.encodeToVector().filter { it < 0 }.sum()
//        return if (white > Math.abs(black)) 0 else 1
//    }
//
//    override fun saveBestNet(nw: Network) {
//        val bestFolder = "$folder/best/"
//        if (File(bestFolder).mkdir()) {
//            println("Создаю каталог $bestFolder")
//        }
//        println("Сохраняю в лучшие сети")
//        NetworkIO().save(nw, "$bestFolder/${Date().time}.net")
//    }
//
//    // переопределяем, чтобы контролировать процесс обучения
//    override fun evoluteEpoch(initPopulation: List<Individual>): List<Individual> {
//        println("эпоха ${++curEpoch}")
//        val start = System.nanoTime()
//        population = super.evoluteEpoch(initPopulation)
//        if (curEpoch % savePerEpoch == 0) saveNets()
//        val fin = System.nanoTime()
//        println("Время: ${(fin-start)/1_000_000} мс\n")
//        return population
//    }
//
//    /** выполняет сохранение сетей на диск **/
//    fun saveNets() {
//        println("saving to $folder...")
//        with(NetworkIO()) {
//            population.forEachIndexed { i, (nw, _) -> save(nw, "$folder/save$i.net") }
//        }
//        if (curEpoch % 100 == 0) testAndClear(listOf("$folder/best/"))
//    }
//}
//
//fun teachNet(layersCapacity: List<Int>, populationSize: Int,
//             epochSize: Int, mutantRate: Double, dir: String): Network {
//    val savePerEpoch = 10.takeIf { epochSize > 10 } ?: epochSize
//    with(EvolutionCheckers(populationSize, layersCapacity, 1, 50, 0, mutantRate, dir, savePerEpoch)) {
//        return evolute(epochSize).nw
//    }
//}
//
//fun main(args: Array<String>) {
//    teachNet(listOf(16, 4), 40, 10000, 0.001, "nets/")
//}
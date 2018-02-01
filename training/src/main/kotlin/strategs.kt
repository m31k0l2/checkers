import java.io.FileWriter

fun teachNet(layersCapacity: List<Int>, populationSize: Int,
             epochSize: Int, mutantRate: Double, dir: String): Network {
    val savePerEpoch = 5.takeIf { epochSize > 5 } ?: epochSize
    with(EvolutionCheckers(populationSize, layersCapacity, 1, 50, 0, mutantRate, dir, savePerEpoch)) {
        return evolute(epochSize).nw
    }
}

fun main(args: Array<String>) {
    var totalEpoch = 0
    while (true) {
        println("~~~ test 1 ~~~~")
        val nw1 = teachNet(listOf(40, 20), 20, 1, 0.1, "testnets/test1/")
        println("~~~ test 2 ~~~~")
        val nw2 = teachNet(listOf(60, 30, 10), 20, 1, 0.1, "testnets/test2/")
        val res1 = play(nw1, nw2)
        val res2 = play(nw2, nw1)
        val s = if (res1 == 0 && res2 == 1) "2"
        else if ((res1 == 0 && res2 == 0) || (res1 == 1 && res2 == 1)) "1"
        else "0"
        val file = FileWriter("stat.txt", true)
        file.write("$s\n")
        file.close()
        println("total epoch: ${++totalEpoch}")
    }
}
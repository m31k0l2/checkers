@file:Suppress("ConstantConditionIf")

import java.io.File

fun main(args: Array<String>) {
    val populationSize = 8
    val epochSize = 10
    var testNet: String?
    val onlyTestNet = false
    val testWinners = true
    val preTrainEpoch = 5
    var totalEpoch = preTrainEpoch
    val list = listOf(
            listOf(40)
    )
    list.forEach {
        teachNet(it, populationSize, preTrainEpoch)
    }
    val myFolder = File("nets/winners")
    val files = myFolder.listFiles()
    val nets = listOf(
            if (testWinners) files.map { "nets/winners/${it.name}" } else listOf(),
            list.map { buildNameForNet(it) }
    ).flatMap { it }
    while (true) {
        list.forEach { layersCapacity ->
            testNet = testNets(nets)
            teachNet(layersCapacity, populationSize,
                    epochSize, testNet?.takeIf { it != buildNameForNet(layersCapacity) }, onlyTestNet)
        }
        totalEpoch += epochSize
        println("Всего эпох: $totalEpoch")
    }
}
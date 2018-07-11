import java.util.*
import kotlin.math.sqrt

interface Network {
    fun activate(x: Checkerboard, alpha: Double): Double
    fun activateConvLayers(layers: List<CNNLayer>, x: Checkerboard): List<Double>
    fun clone(): Network
    val layers: MutableList<Layer>
    fun dropout(layersNumbers: List<Int>, dropoutRate: Double, isRandom: Boolean=false): Network
}

class CNetwork: Network {
    override val layers = mutableListOf<Layer>()
    private val id = ++counterId

    companion object {
        val cnnDividers = listOf(
                MatrixDivider(8,2,1),
                MatrixDivider(7,3,1),
                MatrixDivider(5,2,1))

        val poolDividers = listOf(
                null,
                null,
                MatrixDivider(2,2,2)
        )
        var counterId = -1
    }

    override fun toString() = "$id"

    override fun activateConvLayers(layers: List<CNNLayer>, x: Checkerboard): List<Double> {
        var y = listOf(x.encodeToVector())
        layers.forEach {
            y = it.activate(y)
        }
        return norm(y.flatten())
    }

    private fun activateFullConnectedLayers(layers: List<FullConnectedLayer>, x: List<Double>, alpha: Double): List<Double> {
        var o = x
        layers.forEach {
            o = it.activate(o, alpha)
        }
        return o
    }

    override fun activate(x: Checkerboard, alpha: Double): Double {
        var o = activateConvLayers(layers.filter { it is CNNLayer }.map { it as CNNLayer }, x)
        o = activateFullConnectedLayers(layers.filter { it is FullConnectedLayer }.map { it as FullConnectedLayer }, o, alpha)
        return o.first()
    }

    private fun norm(x: List<Double>): List<Double> {
        val l = sqrt(x.map { it*it }.sum())
        if (l == 0.0) return x
        return x.map { it / l }
    }

    override fun clone() = CNetwork().also { it.layers.addAll(layers.map { it.clone() }) }

    override fun dropout(layersNumbers: List<Int>, dropoutRate: Double, isRandom: Boolean): CNetwork {
        for (l in layersNumbers) {
            for (neuron in layers[l].neurons) {
                for (i in 1..neuron.weights.size) {
                    if (Random().nextDouble() <= dropoutRate)
                        neuron.weights[Random().nextInt(neuron.weights.size)] = if (!isRandom) 0.0 else 1 - 2*Random().nextDouble()
                }
            }
        }
        return this
    }
}

fun Network.convLayer(neuronCount: Int, divider: MatrixDivider, pooler: Pooler?=null) {
    val layer = CNNLayer(divider, pooler, neuronCount)
    layers.add(layer)
}

fun Network.fullConnectedLayer(neuronCount: Int) {
    layers.add(FullConnectedLayer(neuronCount))
}

fun network(init: Network.() -> Unit): Network {
    return CNetwork().apply(init)
}

fun buildNetwork(vararg structure: Int)= network {
    convLayer(structure[0], CNetwork.cnnDividers[0])
    convLayer(structure[1], CNetwork.cnnDividers[1])
    convLayer(structure[2], CNetwork.cnnDividers[2], Pooler(CNetwork.poolDividers[2]!!))
    for (i in 3 until structure.size) {
        fullConnectedLayer(structure[i])
    }
}
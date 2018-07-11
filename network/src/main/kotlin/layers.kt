import java.util.*
import kotlin.math.exp
import kotlin.math.max

class Neuron {
    var weights = mutableListOf<Double>()

    // активационная функция
    fun activate(input: List<Double>, alpha: Double): Double {
        if (weights.isEmpty()) {
            initWeightsRandomValue(input.size + 1)
        } else if (input.size + 1 != weights.size) { // инициализация весов в случае изменения топологии сети
            weights = MutableList(input.size + 1) { if (it < weights.size) weights[it] else 1.0 - 2*Random().nextDouble()  }
        }
        val x = listOf(1.0, *input.toTypedArray()) // добавляем вход 1 для смещения
        return 1/(1 + exp(-alpha*sum(x)))
    }

    fun initWeightsRandomValue(size: Int) {
        weights = MutableList(size) { 1.0 - 2*Random().nextDouble() }
    }

    // сумматор
    fun sum(input: List<Double>): Double {
        var sum = 0.0
        for (i in 0 until input.size) {
            sum += weights[i] * input[i]
        }
        return sum
    }

    fun clone() = Neuron().also { it.weights = weights.map { it }.toMutableList() }
}

interface Layer {
    fun getInstance(): ALayer
    fun clone(): Layer
    val neurons: MutableList<Neuron>
}

abstract class ALayer(size: Int): Layer {
    override val neurons = build(size)
    private fun build(size: Int) = MutableList(size) { Neuron() }
    override fun clone() = getInstance().also {
        it.neurons.addAll(neurons.map { it.clone() }.toMutableList())
    }
}

class CNNLayer(private val matrixDivider: MatrixDivider,
               private val pooler: Pooler?=null, size: Int=0): ALayer(size) {
    override fun getInstance() = CNNLayer(matrixDivider, pooler)

    fun activate(input: List<List<Double>>): List<List<Double>> {
        val x = input.map {
            matrixDivider.divide(it)
        }
        var y = x.flatMap { l ->
            neurons.map { kernel -> l.map {
                if (kernel.weights.size != it.size) kernel.initWeightsRandomValue(it.size)
                kernel.sum(it)
            } }
        }
        y = y.map { relu(it) }
        pooler?.let { y = it.activate(y) }
        return y
    }

    private fun relu(x: List<Double>) = x.map { max(it, 0.0) }
}

class Pooler(private val matrixDivider: MatrixDivider) {
    fun activate(x: List<List<Double>>) = x.map { pool(it, matrixDivider) }

    private fun pool(x: List<Double>, matrixDivider: MatrixDivider) =  matrixDivider.divide(x).map { it.max()!! }
}

class FullConnectedLayer(size: Int=0): ALayer(size) {
    override fun getInstance() = FullConnectedLayer()
    fun activate(input: List<Double>, alpha: Double) = neurons.map { it.activate(input, alpha) }
}

class MatrixDivider(side: Int, size: Int, stride: Int) {
    private val positions: List<List<Int>>
    init {
        positions = (0..side-size step stride).flatMap { posY ->
            (0..side-size step stride).map { posX ->
                (0 until size).flatMap { i -> (0 until size).map { posY*side + posX + it + i * side } }
            }
        }
    }

    fun <E>divide(x: List<E>) = positions.map { it.map { x[it] } }
}
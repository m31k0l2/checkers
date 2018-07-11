import java.io.File
import java.io.FileReader
import java.io.FileWriter

fun Network.save(path: String) {
    val file = FileWriter(File(path))
    layers.forEach {
        if (it is CNNLayer) {
            file.write("CNNLayer\r\n")
        } else if (it is FullConnectedLayer) {
            file.write("layer\r\n")
        }
        it.neurons.forEach {
            file.write("neuron\r\n")
            it.weights.forEach { file.write("$it\r\n") }
        }
    }
    file.write("end")
    file.close()
}

fun Network.load(path: String): Network? {
    val f = File(path)
    if (!f.exists()) return null
    val file = FileReader(f)
    val lines = file.readLines()
    file.close()
    var layer: Layer? = null
    var neuron: Neuron? = null
    lines.forEach { line ->
        when {
            line.contains("CNNLayer") -> {
                layer?.let {
                    it.neurons.add(neuron!!)
                    layers.add(it)
                    neuron = null
                }
                layer = CNNLayer(CNetwork.cnnDividers[layers.size % 5], CNetwork.poolDividers[layers.size % 5]?.let { Pooler(it) })
            }
            line.contains("layer") -> {
                layer?.let {
                    it.neurons.add(neuron!!)
                    layers.add(it)
                    neuron = null
                }
                layer = FullConnectedLayer()
            }
            line == "neuron" -> {
                neuron?.let { layer!!.neurons.add(it) }
                neuron = Neuron()
            }
            line == "end" -> {
                layer!!.neurons.add(neuron!!)
                layers.add(layer!!)
            }
            else -> neuron!!.weights.add(line.toDouble())
        }
    }
    return this
}

fun saveAs(from: String, to: String) {
    CNetwork().load(from)?.save(to)
}
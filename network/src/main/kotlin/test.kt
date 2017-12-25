fun main(args: Array<String>) {
    val population = (1..4).toList()
    val pairs = population.flatMap { individ ->
        population.filter { it != individ }.map { individ to it }
    }.toMutableList()
    (0 until population.size * (population.size - 1) / 2)
            .map { pairs[it] }
            .map { it.second to it.first }
            .forEach { pairs.remove(it) }
    pairs.forEach { println(it) }
}
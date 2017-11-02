fun main(args: Array<String>) {
    val evolution = EvolutionXOR(30, 10)
    val nw = evolution.evolute(500).nw
    evolution.test(nw)
}
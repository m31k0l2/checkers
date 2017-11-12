fun main(args: Array<String>) {
    val game = Game()
    with(game) {
//        test1()
//        test2()
//        test3()
//        test4()
//        test5()
//        test6()
//        test7()
//        test8()
//        test9()
//        test10()
        test11()
    }
}

private fun Game.test1() {
    println("Test 1")
    currentColor = 0
    val whiteCheckers = listOf("e1")
    val blackCheckers = listOf("d2", "b4", "b6")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test2() {
    println("Test 2")
    currentColor = 0
    val whiteCheckers = listOf("e1")
    val blackCheckers = listOf("d2", "c3", "b6")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test3() {
    println("Test 3")
    currentColor = 0
    val whiteCheckers = listOf("e1")
    val blackCheckers = listOf("d2", "d4", "f2", "f4")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test4() {
    println("Test 4")
    currentColor = 0
    val whiteCheckers = listOf("h2")
    val blackCheckers = listOf("b2", "f4", "g7")
    init(whiteCheckers, blackCheckers)
    queen("h2")
    print()
    println(nextMoves())
}

private fun Game.test5() {
    println("Test 5")
    currentColor = 1
    val whiteCheckers = listOf("f2", "c3", "e3", "d4", "f4")
    val blackCheckers = listOf("a5", "d6", "h6", "g7")
    init(whiteCheckers, blackCheckers)
    queen("a5")
    print()
    println(nextMoves())
}

private fun Game.test6() {
    println("Test 6")
    currentColor = 1
    val whiteCheckers = listOf("e1", "f2", "h2")
    val blackCheckers = listOf("c3", "e5", "b6", "d6", "a7", "e7")
    init(whiteCheckers, blackCheckers)
    queen("e1")
    queen("a7")
    print()
    println(nextMoves())
}

private fun Game.test7() {
    println("Test 7")
    currentColor = 0
    val whiteCheckers = listOf("e1", "f2", "h2")
    val blackCheckers = listOf("c3", "e5", "b6", "d6", "a7", "e7")
    init(whiteCheckers, blackCheckers)
    queen("e1")
    queen("a7")
    print()
    println(nextMoves())
}

private fun Game.test8() {
    println("Test 8")
    currentColor = 0
    val whiteCheckers = listOf("g1", "c1", "a1", "h2", "d2", "g3", "e3", "f4", "d4", "c5")
    val blackCheckers = listOf("h8", "d8", "b8", "g7", "e7", "c7", "a7", "b6", "g5")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test9() {
    println("Test 9")
    currentColor = 1
    val whiteCheckers = listOf("g1", "c1", "a1", "h2", "d2", "g3", "e3", "d4", "c5", "d6", "f6")
    val blackCheckers = listOf("h8", "d8", "b8", "c7", "a7", "b6")
    init(whiteCheckers, blackCheckers)
    queen("d6")
    print()
    println(nextMoves())
}

private fun Game.test10() {
    println("Test 10")
    currentColor = 0
    val whiteCheckers = listOf("a1", "c1", "e1", "g1", "b2", "d2", "f2", "h2", "g3", "b4", "f4")
    val blackCheckers = listOf("b8", "d8", "a7", "c7", "b6", "d6", "f6")
    init(whiteCheckers, blackCheckers)
    print()
    println(nextMoves())
}

private fun Game.test11() {
    println("Test 10")
    currentColor = 0
    val whiteCheckers = listOf("a1", "c1", "e1", "g1", "b2", "d2", "a3", "g3", "b4", "g5")
    val blackCheckers = listOf("b8", "d8", "h8", "a7", "c7", "g7", "b6", "d6", "f6")
    init(whiteCheckers, blackCheckers)
    print()
    val step = nextMoves().first()
    println(step)
    go(step)
    print()
}
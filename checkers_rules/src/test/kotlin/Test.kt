class Test {
    private val debug = false

    private fun searchMoves(color: Int, whiteCheckers: List<String>, blackCheckers: List<String>, queens: List<String>? = null): String = with(GameController()) {
        init(color, whiteCheckers, blackCheckers, queens)
        val moves = nextMoves()
        val test = moves.reduceRight { a, b -> "$a,$b" }
        if (debug) {
            print()
            println(test)
        }
        test
    }

    private fun GameController.init(color: Int, whiteCheckers: List<String>, blackCheckers: List<String>, queens: List<String>?) {
        currentColor = color
        init(whiteCheckers, blackCheckers)
        queens?.forEach { queen(it) }
    }

    private fun go(color: Int, whiteCheckers: List<String>, blackCheckers: List<String>, queens: List<String>?, move: String): List<String> = with(GameController()) {
        init(color, whiteCheckers, blackCheckers, queens)
        go(move)
        val white = checkerboard.getCheckers(0).map { it.toString() }.reduceRight { a, b -> "$a,$b" }
        val black = checkerboard.getCheckers(1).map { it.toString() }.reduceRight { a, b -> "$a,$b" }
        listOf(white, black)
    }

    @Test
    fun test1() = assertEquals(searchMoves(0, listOf("e1"), listOf("d2", "b4", "b6")), "e1:c3:a5:c7")

    @Test
    fun test2() = assertEquals(searchMoves(0, listOf("e1"), listOf("d2", "c3", "b6")), "e1-f2")

    @Test
    fun test3() = assertEquals(searchMoves(0, listOf("e1"), listOf("d2", "d4", "f2", "f4")), "e1:c3:e5:g3:e1,e1:g3:e5:c3:e1")

    @Test
    fun test4() = assertEquals(searchMoves(0, listOf("h2"), listOf("b2", "f4", "g7"), listOf("h2")), "h2:e5:a1,h2:e5:h8")

    @Test
    fun test5() = assertEquals(searchMoves(1, listOf("f2", "c3", "e3", "d4", "f4"), listOf("a5", "d6", "h6", "g7"), listOf("a5")), "a5:e1:g3:e5")

    @Test
    fun test6() = assertEquals(searchMoves(1, listOf("e1", "f2", "h2"), listOf("c3", "e5", "b6", "d6", "a7", "e7"), listOf("e1", "a7")), "c3-b2,c3-d2,e5-d4,e5-f4,b6-a5,b6-c5,d6-c5,a7-b8,e7-f6")

    @Test
    fun test7() = assertEquals(searchMoves(0, listOf("e1", "f2", "h2"), listOf("c3", "e5", "b6", "d6", "a7", "e7"), listOf("e1", "a7")), "e1:a5:d8:f6:d4")

    @Test
    fun test8() = assertEquals(searchMoves(0, listOf("g1", "c1", "a1", "h2", "d2", "g3", "e3", "f4", "d4", "c5"), listOf("h8", "d8", "b8", "g7", "e7", "c7", "a7", "b6", "g5")), "f4:h6:f8:d6")

    @Test
    fun test9() = assertEquals(searchMoves(1, listOf("g1", "c1", "a1", "h2", "d2", "g3", "e3", "d4", "c5", "d6", "f6"), listOf("h8", "d8", "b8", "c7", "a7", "b6"), listOf("d6")), "c7:e5:c3:e1:h4:e7,c7:e5:g7")

    @Test
    fun test10() = assertEquals(searchMoves(0, listOf("a1", "c1", "e1", "g1", "b2", "d2", "f2", "h2", "g3", "b4", "f4"), listOf("b8", "d8", "a7", "c7", "b6", "d6", "f6")), "b2-a3,b2-c3,d2-c3,d2-e3,f2-e3,g3-h4,b4-a5,b4-c5,f4-e5,f4-g5")

    @Test
    fun test11() {
        val whiteCheckers = listOf("a1", "c1", "e1", "g1", "b2", "d2", "a3", "g3", "b4", "g5")
        val blackCheckers = listOf("b8", "d8", "h8", "a7", "c7", "g7", "b6", "d6", "f6")
        assertEquals(searchMoves(0, whiteCheckers, blackCheckers), "g5:e7:c5")
        assertEquals(go(0, whiteCheckers, blackCheckers, null, "g5:e7:c5"), listOf("a1,c1,e1,g1,b2,d2,a3,g3,b4,c5", "b6,a7,c7,g7,b8,d8,h8"))
    }

    @Test
    fun test12() {
        val whiteCheckers = listOf("a1", "c1", "e1", "g1", "b2", "d2", "f2", "h2", "e3", "b4", "d4", "h4")
        val blackCheckers = listOf("b8", "d8", "f8", "h8", "a7", "c7", "e7", "g7", "d6", "f6", "a5", "g5")
        assertEquals(searchMoves(1, whiteCheckers, blackCheckers), "a5:c3:e5")
        assertEquals(go(1, whiteCheckers, blackCheckers, null, "a5:c3:e5"), listOf("a1,c1,e1,g1,b2,d2,f2,h2,e3,h4", "e5,g5,d6,f6,a7,c7,e7,g7,b8,d8,f8,h8"))
    }

    @Test
    fun test13() {
        val whiteCheckers = listOf("c1", "b2", "d2", "f2", "a3", "e3", "f4", "a5", "f6")
        val blackCheckers = listOf("b8", "f8", "h8", "c7", "b6", "d6", "h6", "c5", "e5", "d4")
        assertEquals(searchMoves(1, whiteCheckers, blackCheckers), "e5:g3:e1:c3:a1,e5:g7")
        assertEquals(go(1, whiteCheckers, blackCheckers, null, "e5:g3:e1:c3:a1"), listOf("c1,a3,e3,a5,f6", "a1,d4,c5,b6,d6,h6,c7,b8,f8,h8"))
    }

    @Test
    fun test14() {
        val whiteCheckers = listOf("a1", "g1", "h4", "g5", "b8")
        val blackCheckers = listOf("c5", "e5", "a3")
        assertEquals(searchMoves(0, whiteCheckers, blackCheckers, listOf("b8")), "b8:f4,b8:g3,b8:h2")
        assertEquals(go(0, whiteCheckers, blackCheckers, listOf("b8"), "b8:f4"), listOf("a1,g1,f4,h4,g5", "a3,c5"))
    }

    @Test
    fun test15() {
        val whiteCheckers = listOf("c1", "e1", "g1", "g3")
        val blackCheckers = listOf("b8", "d8", "f8", "h8", "a7", "e7", "h6", "h4")
        assertEquals(searchMoves(1, whiteCheckers, blackCheckers, listOf("h4")), "h4:f2")
        assertEquals(go(1, whiteCheckers, blackCheckers, listOf("h4"), "h4:f2"), listOf("c1,e1,g1", "f2,h6,a7,e7,b8,d8,f8,h8"))
    }
}
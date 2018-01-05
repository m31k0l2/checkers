import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec

class MyTests : StringSpec() {
    private val debug = true

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

    init {
        "Атака шашкой нескольких подряд" {
            searchMoves(0, listOf("e1"), listOf("d2", "b4", "b6")) shouldBe "e1:c3:a5:c7"
        }

        "Атака невозможна" {
            searchMoves(0, listOf("e1"), listOf("d2", "c3", "b6")) shouldBe "e1-f2"
        }
        "Турецкий удар" {
            searchMoves(0, listOf("e1"), listOf("d2", "d4", "f2", "f4")) shouldBe  "e1:c3:e5:g3:e1,e1:g3:e5:c3:e1"
        }
    }
}
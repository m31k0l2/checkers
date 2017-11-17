/**
 * Создаёт свертки доски
 * Свёртки определяются квадратами со сторонами от 3х до 8
 * 8 означает что свертка представляет собой всю доску
 * Свертки накладываются друг на друга, перекрывая себя, перемещаются на одно поле
 */
class InputEncoder {
    private fun <T> conv(input: List<T>, x0: Int, size: Int) = List(size, { i: Int -> List(size, { input[x0 + it + 8 * i] }) }).flatMap { it }

    private fun <T> convMatrix(input: List<T>, size: Int) = (0..8 - size).map { y ->
        (0..8 - size).map { x ->
            conv(input, x + y * 8, size)
        }
    }.flatMap { it }

    private fun convInput(vector: List<Double>, size: Int): List<List<Double>> {
        val matrix = (0..7).map { vector.subList(it * 4, (it + 1) * 4) }
        val board = matrix.mapIndexed { i, row ->
            row.map {
                if (i % 2 == 0) listOf(null, it) else {
                    listOf(it, null)
                }
            }.flatMap { it }
        }.flatMap { it }
        return convMatrix(board, size).map {
            it.filterNotNull()
        }.filter { it.isNotEmpty() }
    }

    fun encode(vector: List<Double>) = (3..8).flatMap { convInput(vector, it) }
}
class Position(val strPosition: String) {
    val col = gameConstants.letters.indexOf(strPosition.substring(0, 1))
    val row = strPosition.substring(1, 2).toInt()-1
    override fun toString(): String {
        return strPosition
    }

    constructor(col: Int, row: Int) : this(gameConstants.letters[col]+(row+1))

    fun leftAndUp(): Position? {
        if (col - 1 < 0 || row + 1 > 7) return null
        return Position(col-1, row+1)
    }

    fun rightAndUp(): Position? {
        if (col + 1 > 7 || row + 1 > 7) return null
        return Position(col+1, row+1)
    }

    fun leftAndDown(): Position? {
        if (col - 1 < 0 || row - 1 < 0) return null
        return Position(col-1, row-1)
    }

    fun rightAndDown(): Position? {
        if (col + 1 > 7 || row - 1 < 0) return null
        return Position(col+1, row-1)
    }

    fun moveByDirection(dir: Direction): Position? {
        return when (dir) {
            Direction.NW -> leftAndUp()
            Direction.NE -> rightAndUp()
            Direction.SW -> leftAndDown()
            Direction.SE -> rightAndDown()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Position

        if (col != other.col) return false
        if (row != other.row) return false

        return true
    }

    override fun hashCode() = (col+1)*(row+1)
}
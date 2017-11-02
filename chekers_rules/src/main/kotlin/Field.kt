data class Field(val color: Int, val position: Position, var checker: Checker? = null) // whiteAI = 0; blackAI = 1
{
    override fun toString(): String {
        return position.strPosition
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Field

        if (color != other.color) return false
        if (position != other.position) return false
        if (checker != other.checker) return false

        return true
    }
}
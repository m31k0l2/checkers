data class Checker(val color: Int, var field: Field? = null, var queen: Boolean = false, var id: Int = Checker.gen_id++) {
    companion object {
        var gen_id = 0
    }

    override fun hashCode(): Int {
        var result = if (queen) 65 else 0
        if (field != null) {
            result += field!!.position.hashCode()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Checker
        return id == other.id
    }
}
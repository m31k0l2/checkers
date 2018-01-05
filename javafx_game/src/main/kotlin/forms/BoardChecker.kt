import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

class BoardChecker(private val position: Position, val color: Int, val isQueen: Boolean, orientation: Int = 0) : Group() {
    private val size = BoardPane.size
    var orientation = orientation
        set(value) {
            field = value
            val center = calcCenter()
            children.map { it as Circle }.forEach {
                it.centerX = center.first
                it.centerY = center.second
            }
        }

    init {
        draw()
    }

    private fun calcCenter(): Pair<Double, Double> {
        val x: Double
        val y: Double
        if (orientation == 0) {
            x = (position.x-1) * size + size / 2
            y = (7 - (position.y-1)) * size + size / 2
        } else {
            x = (7 - (position.x-1)) * size + size / 2
            y = (position.y-1) * size + size / 2
        }
        return x to y
    }

    private fun draw() {
        val fillColor = Color.WHITE.takeIf { color == 0 } ?: Color.BLACK
        val strokeColor = Color.BLACK.takeIf { color == 0 } ?: Color.WHITE
        val (x, y) = calcCenter()
        val circle = Circle(x, y, size / 2 - 5)
        circle.fill = fillColor
        circle.stroke = strokeColor
        children.add(circle)
        if (isQueen) {
            val circle2 = Circle(x, y, size / 2 - 10)
            circle2.fill = fillColor
            circle2.stroke = strokeColor
            children.add(circle2)
        }
        toFront()
    }
}
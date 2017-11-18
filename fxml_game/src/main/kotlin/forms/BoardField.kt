import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text

class BoardField(val position: Position, orientation: Int=0) : Group() {
    var orientation = orientation
        set(value) {
            field = value
            val (x, y) = insertPoint()
            rectangle.x = x
            rectangle.y = y
            children.filter { it is Text }.map { it as Text }.forEach {
                it.x = x + 5; it.y = y + 25
            }
        }
    val size = BoardPane.size
    var checker: BoardChecker? = null
        set(value) {
            field = value
            if (value != null) {
                children.add(value)
            } else {
                val filter = children.filter { it is BoardChecker }
                children.removeAll(filter)
            }
        }
    val rectangle = drawRectangle()

    init {
        children.add(rectangle)
        onMouseClicked = EventHandler {
            BoardPane.model.onClicked(position)
        }
    }

    fun insertPoint(): Pair<Double, Double> {
        val x: Double
        val y: Double
        if (orientation == 0) {
            x = (position.x-1) * size
            y = (7 - (position.y-1)) * size
        } else {
            x = (7 - (position.x-1)) * size
            y = (position.y-1) * size
        }
        return x to y
    }

    fun drawRectangle() = Rectangle().apply {
        val insertPoint = insertPoint()
        x = insertPoint.first; y = insertPoint.second
        width = size
        height = size
        fill = Color.BURLYWOOD
        stroke = Color.BLACK
    }

    fun fill(color: Color) {
        rectangle.fill = color
    }
}
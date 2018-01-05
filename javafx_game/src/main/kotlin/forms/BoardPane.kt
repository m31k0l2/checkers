import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text

class BoardPane : Pane() {
    companion object {
        val size = 50.0
        lateinit var model: BoardModel
    }

    init {
        drawDesk()
        init(0)
    }

    fun init(orientation: Int) {
        model = BoardModel(this)
        model.botColor = 1 - orientation
        children.removeAll(children.filter { it is Text })
        val checkerboard = GameController().checkerboard
        checkerboard.board.filter { it.color == 1 }.map { Position(it.x, it.y) }.forEach { pos ->
            model.fields.put(pos.toString(), BoardField(pos, orientation))
            val checker = checkerboard.get(pos)?.checker ?: return@forEach
            val boardChecker = BoardChecker(pos, checker.color, checker.type == 1, orientation)
            model.fields[pos.toString()]!!.checker = boardChecker
            model.checkers.put(pos.toString(), boardChecker)
        }
        children.addAll(model.fields.values)
        if (orientation == 1) model.botStep()
    }

    private fun drawDesk() {
        val rect = Rectangle(0.0, 0.0, size*8, size*8)
        rect.fill = Color.BEIGE
        rect.stroke = Color.BLACK
        children.add(rect)
    }
}
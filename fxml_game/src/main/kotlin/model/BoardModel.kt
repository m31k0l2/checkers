import javafx.animation.PathTransition
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration


class BoardModel(val desk: BoardPane) {
    val fields = mutableMapOf<String, BoardField>()
    val checkers = mutableMapOf<String, BoardChecker>()
    var currentPlayerColor = 0
    var agentColor = 1
    var game = Game()
    var availableSteps: List<String>
    var activeFields: Set<String>
    var availableMoveFields: Set<String>? = null
    var from: String? = null
    var stepCounter = 0.0
    val stepLimit = 50
    val agent = Player(NetworkIO().load("best.net")!!, 2)
    var animationClose = false

    init {
        availableSteps = game.nextMoves()
        activeFields = extractActiveFields()
    }

    fun onClicked(position: Position) {
        if (currentPlayerColor == agentColor) return
        // снять выделение
        clearSelected()
        // пометить доступные для хода фигура
        activeFields.forEach {
            fields[it]!!.fill(if (it == position.toString()) Color.BLUE else Color.YELLOW)
        }
        if (position.toString() in activeFields) {
            // пометить доступные ходы для выбранной фигуры
            availableMoveFields = getCheckerMoveFields(position.toString()).apply {
                forEach { fields[it]!!.fill(Color.GREEN) }
            }
            from = position.toString()
        } else {
            availableMoveFields?.let {
                val to = position.toString()
                if (to in it) {
                    nextStep(getCommand(from!!, to))
                    clearSelected()
                    agentStep()
                }
            }
            availableMoveFields = null
            from = null
        }
    }

    fun agentStep() {
        Thread(Runnable {
            Thread.sleep(600L)
            val step = agent.selectMove(game.checkerboard, currentPlayerColor, availableSteps)
            Platform.runLater({
                nextStep(step)
            })
        }).start()
    }

    fun nextStep(command: String) {
        animationClose = false
        if (stepCounter.toInt() == stepLimit) {
            win(-1)
            return
        }
        stepCounter += 0.5
        game.go(command)
        showTrack(command)
        // перевести ход на соперника
        currentPlayerColor = 1 - currentPlayerColor
        game.currentColor = currentPlayerColor
        availableSteps = game.nextMoves()
        if (availableSteps.isEmpty()) {
            win(1 - game.currentColor)
        }
        activeFields = extractActiveFields()
        if (availableSteps.isEmpty()) {
            win(1 - currentPlayerColor)
        }
        game.print()
    }

    fun showTrack(command: String) {
        val positions = if (command.contains("-")) command.split("-")
        else command.split(":")
        val path = Path()
        positions.mapIndexed { i, pos ->
            val field = fields[pos] ?: return
            val (x, y) = field.insertPoint()
            if (i == 0) path.elements.add(MoveTo(x+25, y+25))
            else path.elements.add(LineTo(x+25, y+25))
        }
        val from = fields[positions.first()]!!
        val checker = from.checker!!
        from.checker = null
        desk.children.add(checker.apply { toFront() })
        val transform = PathTransition()
        transform.duration = Duration.seconds(0.4)
        transform.node = checker
        transform.path = path
        transform.cycleCount = 1
        transform.isAutoReverse = true
        transform.orientation = PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT
        transform.onFinished = EventHandler {
            animationClose = true
        }
        transform.play()
        transform.onFinished = EventHandler {
            desk.children.remove(checker)
            checkerBoardUpdate()
        }
    }

    fun win(color: Int) {
        val text: String
        if (color > -1) {
            if (color == 0) text = "   Белые\nпобедили"
            else text = " Черные\nпобедили"
            fields.forEach { _, boardField -> boardField.onMouseClicked = null }
        } else {
            text = "\n  ничья"
        }
        desk.children.add(Text(100.0, 150.0, text).apply { font = Font.font(50.0); fill = Color.RED })
        clearSelected()
    }

    fun clearSelected() = fields.values.forEach { it.fill(Color.BURLYWOOD) }

    private fun checkerBoardUpdate() {
        fields.forEach { pos, boardField ->
            val field = game.checkerboard.get(pos)!!
            field.checker?.let { (color, type) ->
                val queen = type == 1
                boardField.checker?.let {  boardChecker ->
                    if (boardChecker.color != color && boardChecker.isQueen != queen) {
                        boardField.checker = BoardChecker(Position(pos), color, queen, boardField.orientation)
                    }
                } ?: run {
                    boardField.checker = BoardChecker(Position(pos), color, queen, boardField.orientation)
                }
            } ?: run {
                boardField.checker = null
            }
        }
    }

    private fun extractActiveFields() = availableSteps.map { it.substring(0, 2) }.toSet()

    fun getCheckerMoveFields(position: String) = availableSteps
            .filter { it.substring(0, 2) == position }
            .map { it.substring(it.length-2, it.length) }.toSet()

    fun getCommand(from: String, to: String) = availableSteps
            .filter { it.substring(0, 2) == from }
            .filter { it.substring(it.length-2, it.length) == to }.first()
}

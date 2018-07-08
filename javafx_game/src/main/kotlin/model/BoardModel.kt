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
import java.io.File

fun bot(state: Checkerboard, color: Int): String {
    val node = alphaBetaSearch(Node(state, color = color))
    println(node?.value)
    return node!!.action
}

class BoardModel(private val desk: BoardPane) {
    val fields = mutableMapOf<String, BoardField>()
    val checkers = mutableMapOf<String, BoardChecker>()
    var botColor = 1
    var game = GameController()
    var activeFields: Set<String>
    var availableMoveFields: Set<String>? = null
    var from: String? = null
    var stepCounter = 0.0
    private val stepLimit = 50
//    private val netPath = getPath("nets/16-4/")
//    private val bot = Player(NetworkIO().load(netPath)!!, 2, 3.0, true)
    var animationClose = false
    private var moves: List<String>

//    private fun getPath(folderName: String): String {
//        val dir = File(folderName)
//        return dir.listFiles().sorted().last().toString()
//    }

    init {
        moves = game.nextMoves()
        activeFields = extractActiveFields()
    }

    fun onClicked(position: Position) {
        if (game.currentColor == botColor) return
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
                    botStep()
                }
            }
            availableMoveFields = null
            from = null
        }
    }

    fun botStep() {
        Thread(Runnable {
            Thread.sleep(600L)
            if (moves.isEmpty()) return@Runnable
//            val step = bot.selectMove(game.checkerboard, game.currentColor, moves)
            Platform.runLater {
                val step = bot(game.checkerboard, game.currentColor)
                nextStep(step)
            }
        }).start()
    }

    private fun nextStep(command: String) {
        animationClose = false
        if (stepCounter.toInt() == stepLimit) {
            win(-1)
            return
        }
        stepCounter += 0.5
        println("Step: $stepCounter")
        game.go(command)
        println("${game.currentColor}: $command")
        game.print()
        showTrack(command)
        // перевести ход на соперника
        game.currentColor = 1 - game.currentColor
        moves = game.nextMoves()
        if (moves.isEmpty()) {
            win(1 - game.currentColor)
        } else activeFields = extractActiveFields()
    }

    private fun showTrack(command: String) {
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

    /** Отобразить победителя **/
    private fun win(color: Int) {
        val text: String
        if (color > -1) {
            text = if (color == 0) "   Белые\nпобедили"
            else " Черные\nпобедили"
            fields.forEach { _, boardField -> boardField.onMouseClicked = null }
            if (color == botColor) {
//                NetworkIO().save(bot.nw, "nets/winners/${bot.nw.hashCode()}.net")
            }
        } else {
            text = "\n  ничья"
        }
        desk.children.add(Text(100.0, 150.0, text).apply { font = Font.font(50.0); fill = Color.RED })
        clearSelected()
    }

    /** Очистить выделение полей на доске **/
    private fun clearSelected() = fields.values.forEach { it.fill(Color.BURLYWOOD) }

    /** Отобразить состояние доски **/
    private fun checkerBoardUpdate() {
        fields.forEach { pos, boardField ->
            val field = game.checkerboard.get(pos)!!
            field.checker?.let { (color, type) ->
                val queen = type == 1
                boardField.checker?.let { boardChecker ->
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

    /** Возвращает список позиций шашек, которые могут ходить **/
    private fun extractActiveFields() = moves.map { it.substring(0, 2) }.toSet()

    /** Возвращает список позиций полей на которых оканчиваются ходы шашки с позицией [position] **/
    private fun getCheckerMoveFields(position: String) = moves
            .filter { it.substring(0, 2) == position }
            .map { it.substring(it.length-2, it.length) }.toSet()

    /** Возвращает команду для шашки с позиции [from], которая перемещается на позицию [to] **/
    private fun getCommand(from: String, to: String) = moves
            .filter { it.substring(0, 2) == from }.first { it.substring(it.length - 2, it.length) == to }
}

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.stage.Stage

@Suppress("JAVA_CLASS_ON_COMPANION")
class Main : Application() {
    lateinit var board: BoardPane
    lateinit var numberPaneLeft: Pane
    lateinit var numberPaneRight: Pane
    lateinit var lettersTopPane: Pane
    lateinit var lettersBottomPane: Pane
    private val letters = listOf("a", "b", "c", "d", "e", "f", "g", "h")

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Шашки"
        val root = BorderPane().apply {
            top = VBox().apply { children.addAll(buildMenu(primaryStage.widthProperty()), buildDesk()) }
        }
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

    private fun buildMenu(widthProperty: ReadOnlyDoubleProperty) = MenuBar().apply {
        prefWidthProperty().bind(widthProperty)
        val fileMenu = Menu("Игра")
        val playForWhiteMenuItem = MenuItem("Играть за белых").apply {
            setOnAction {
                changeSides(0)
            }
        }
        val playForBlackMenuItem = MenuItem("Играть за черных").apply {
            setOnAction {
                changeSides(1)
            }
        }
        val exitMenuItem = MenuItem("Выход").apply {
            setOnAction { Platform.exit() }
        }
        fileMenu.items.addAll(playForWhiteMenuItem, playForBlackMenuItem,
                SeparatorMenuItem(), exitMenuItem)
        menus.addAll(fileMenu)
    }

    private fun changeSides(orientation: Int) {
        board.init(orientation)
        numberPaneLeft.children.apply {
            clear()
            addAll(buildNumbers(orientation))
        }
        numberPaneRight.children.apply {
            clear()
            addAll(buildNumbers(orientation))
        }
        lettersTopPane.children.apply {
            clear()
            addAll(buildLetters(orientation))
        }
        lettersBottomPane.children.apply {
            clear()
            addAll(buildLetters(orientation))
        }
    }

    private fun buildNumbers(orientation: Int) = (0..7).map {
        Text(10.0, 30.0 + 50 * it, "${if (orientation == 0) 8-it else it+1}")
                .apply { font = Font(20.0) }
    }

    private fun buildNumberPane() = Pane().apply {
        padding = Insets(0.0, 10.0, 0.0, 0.0)
        background = Background(BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY))
        children.addAll(buildNumbers(0))
    }

    private fun buildLettersPane() = Pane().apply {
        padding = Insets(10.0, 0.0, 0.0, 0.0)
        background = Background(BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY))
        val letters = buildLetters(0)
        children.addAll(letters)
    }

    private fun buildLetters(orientation: Int) = (0..7).map {
        Text(50.0 + 50 * it, 20.0,
            letters[if (orientation == 0) it else 7-it])
                .apply { font = Font(20.0) }
    }

    private fun buildDesk() = Group().apply {
        children.add(VBox().apply {
            lettersTopPane = buildLettersPane()
            lettersBottomPane = buildLettersPane()
            children.addAll(
                    lettersTopPane,
                    HBox().apply {
                        board = BoardPane()
                        numberPaneLeft = buildNumberPane()
                        numberPaneRight = buildNumberPane()
                        children.addAll(numberPaneLeft, board, numberPaneRight)
                    },
                    lettersBottomPane
            )
        })
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}

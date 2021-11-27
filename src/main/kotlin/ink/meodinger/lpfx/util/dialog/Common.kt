package ink.meodinger.lpfx.util.dialog

import ink.meodinger.lpfx.DIALOG_HEIGHT
import ink.meodinger.lpfx.DIALOG_WIDTH
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.image.resizeByRadius
import ink.meodinger.lpfx.util.string.omitHighText
import ink.meodinger.lpfx.util.string.omitWideText
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.resource.loadAsImage
import ink.meodinger.lpfx.io.LogSender

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.stage.Window
import javafx.util.Callback
import java.util.*
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/8/8
 * Have fun with my code!
 */

/**
 * Constant
 */
const val DIALOG_ICON_RADIUS = 32.0

val confirmImageView = ImageView(loadAsImage("/file/image/dialog/Confirm.png").resizeByRadius(DIALOG_ICON_RADIUS))
val infoImageView    = ImageView(loadAsImage("/file/image/dialog/Info.png").resizeByRadius(DIALOG_ICON_RADIUS))
val alertImageView   = ImageView(loadAsImage("/file/image/dialog/Alert.png").resizeByRadius(DIALOG_ICON_RADIUS))
val errorImageView   = ImageView(loadAsImage("/file/image/dialog/Error.png").resizeByRadius(DIALOG_ICON_RADIUS))
fun showDialog(graphic: Node?, title: String, header: String?, content: String, owner: Window?, vararg buttonTypes: ButtonType): Optional<ButtonType> {
    val dialog = Dialog<ButtonType>()
    dialog.initOwner(owner)
    dialog.graphic = graphic
    dialog.title = title
    dialog.headerText = header
    dialog.contentText = omitWideText(omitHighText(content), dialog.width / 3 * 2)
    dialog.dialogPane.buttonTypes.addAll(buttonTypes)
    dialog.dialogPane.setOnMouseClicked {
        if (it.clickCount > 1) {
            val maxWidth = dialog.width / 3 * 2
            val nowText = Text(dialog.contentText).apply { wrappingWidth = maxWidth }
            val newText = Text(content).apply { wrappingWidth = maxWidth }
            val expandHeight = newText.boundsInLocal.height - nowText.boundsInLocal.height
            dialog.contentText = content
            dialog.height += expandHeight
        }
    }

    return dialog.showAndWait()
}

/**
 * Show message for confirm
 * @param content Message to show
 * @return ButtonType? YES | NO
 */
fun showConfirm(content: String, owner: Window?): Optional<ButtonType> {
    return showConfirm(I18N["common.confirm"], null, content, owner)
}
/**
 * Show message for confirm
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | NO
 */
fun showConfirm(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(confirmImageView, title, header, content, owner, ButtonType.YES, ButtonType.NO)
}

/**
 * Show information
 * @param content Info to show
 * @return ButtonType? OK
 */
fun showInfo(content: String, owner: Window?): Optional<ButtonType> {
    return showInfo(I18N["common.info"], null, content, owner)
}
/**
 * Show information
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? OK
 */
fun showInfo(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(infoImageView, title, header, content, owner, ButtonType.OK)
}

/**
 * Show alert
 * @param content Alert to show
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(content: String, owner: Window?): Optional<ButtonType> {
    return showAlert(I18N["common.alert"], null, content, owner)
}
/**
 * Show alert
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(alertImageView, title, header, content, owner, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
}

/**
 * Show error
 * @param content Error to show
 * @return ButtonType? OK
 */
fun showError(content: String, owner: Window?): Optional<ButtonType> {
    return showError(I18N["common.error"], null, content, owner)
}
/**
 * Show error
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @param owner Owner window
 * @return ButtonType? Ok
 */
fun showError(title: String, header: String?, content: String, owner: Window?): Optional<ButtonType> {
    return showDialog(errorImageView, title, header, content, owner, ButtonType.OK)
}

/**
 * Show stack trace in expandable content
 * @param e Exception to print
 * @return ButtonType? OK
 */
fun showException(e: Throwable, owner: Window?): Optional<ButtonType> {

    // Get exception stack trace
    val text = e.stackTraceToString()

    // Create pane
    val content = VBox(8.0)

    val sentLabel = Label().also {
        it.padding = Insets(0.0, 8.0, 0.0, 0.0)
    }
    val header = HBox(
        Label(omitWideText(e.message ?: e.javaClass.name, 400.0)),
        HBox().also { HBox.setHgrow(it, Priority.ALWAYS) },
        sentLabel,
        Button(I18N["logs.button.send"]) does {
            LogSender.sendLog(Logger.log)
            sentLabel.text = I18N["logs.sent"]
        }
    ).also {
        it.alignment = Pos.CENTER_LEFT
    }
    val textArea = TextArea(text).also {
        it.isEditable = false
        it.prefWidthProperty().bind(content.widthProperty())
        it.prefHeightProperty().bind(content.heightProperty())
    }

    content.children.addAll(header, Separator(), Label("The exception stacktrace is:"), textArea)

    val dialog = Dialog<ButtonType>().also { it.initOwner(owner) }

    dialog.title = I18N["common.error"]
    dialog.isResizable = true
    dialog.headerText = e.javaClass.name
    dialog.dialogPane.prefWidth = DIALOG_WIDTH
    dialog.dialogPane.prefHeight = DIALOG_HEIGHT
    dialog.dialogPane.content = content
    dialog.dialogPane.buttonTypes.add(ButtonType.OK)

    return dialog.showAndWait()
}

// Specific dialogs

/**
 * Show a link
 * @param owner Owner
 * @param title Dialog title
 * @param header Dialog header
 * @param content Dialog content
 * @param link Link text
 * @param handler Handler for link
 * @return ButtonType? OK
 */
fun showLink(owner: Window?, title: String, header: String?, content: String?, link: String, handler: EventHandler<ActionEvent>): Optional<ButtonType> {
    val dialog = Dialog<ButtonType>()
    dialog.initOwner(owner)

    val label = Label(content)
    val separator = Separator().also { it.padding = Insets(8.0, 0.0, 8.0, 0.0) }
    val hyperlink = Hyperlink(link).also { it.onAction = handler; it.padding = Insets(0.0) }
    val box = VBox(label, separator, hyperlink)

    dialog.title = title
    dialog.headerText = header
    dialog.dialogPane.buttonTypes.add(ButtonType.OK)
    dialog.dialogPane.content = box

    return dialog.showAndWait()
}

fun showInput(owner: Window?, title: String, header: String, placeholder: String?, formatter: TextFormatter<String>?): Optional<String> {
    val dialog = TextInputDialog(placeholder)
    dialog.initOwner(owner)
    dialog.title = title
    dialog.headerText = header
    dialog.editor.textFormatter = formatter
    return dialog.showAndWait()
}

fun showInputArea(owner: Window?, title: String, placeholder: String): Optional<String> {
    val dialog = Dialog<String>()
    dialog.initOwner(owner)
    dialog.title = title

    val textArea = TextArea(placeholder)
    dialog.dialogPane.content = HBox(textArea)
    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

    dialog.resultConverter = Callback { buttonType ->
        if (buttonType == ButtonType.OK) textArea.text
        else placeholder
    }
    return dialog.showAndWait()
}

fun <T> showChoice(owner: Window?, title: String, header: String, choices: List<T>): Optional<T> {
    val dialog = ChoiceDialog(choices[0], choices)
    dialog.initOwner(owner)
    dialog.title = title
    dialog.contentText = header
    return dialog.showAndWait()
}

fun <T> showChoiceList(owner: Window?, unselected: List<T>, selected: List<T> = ArrayList()): Optional<List<T>> {
    val dialog = Dialog<List<T>>()
    dialog.initOwner(owner)
    dialog.title = I18N["util.dialog.choose.title"]

    val left = ListView<T>()
    val right = ListView<T>()
    left.selectionModel.selectionMode = SelectionMode.MULTIPLE
    right.selectionModel.selectionMode = SelectionMode.MULTIPLE
    left.items.addAll(unselected)
    right.items.addAll(selected)

    val add = Button(I18N["util.dialog.choose.add"])
    val addAll = Button(I18N["util.dialog.choose.add_all"])
    val remove = Button(I18N["util.dialog.choose.remove"])
    val removeAll = Button(I18N["util.dialog.choose.remove_all"])
    val vBox = VBox(add, addAll, remove, removeAll).also { it.alignment = Pos.CENTER }
    for (b in vBox.children) {
        val btn = b as Button
        btn.prefWidth = 48.0
        btn.prefHeight = 48.0
    }

    val mover: (ListView<T>, ListView<T>) -> Unit = { from, to ->
        ArrayList(from.selectionModel.selectedItems).forEach { item ->
            from.items.remove(item)
            to.items.add(item)
        }
    }
    val moverAll: (ListView<T>, ListView<T>) -> Unit = { from, to ->
        to.items.addAll(from.items)
        from.items.clear()
    }

    add.onAction = EventHandler { mover(left, right) }
    addAll.onAction = EventHandler { moverAll(left, right) }
    remove.onAction = EventHandler { mover(right, left) }
    removeAll.onAction = EventHandler { moverAll(right, left) }

    val pane = GridPane()
    pane.add(Label(I18N["util.dialog.choose.potential"]), 0, 0)
    pane.add(Label(I18N["util.dialog.choose.selected"]), 2, 0)
    pane.add(left, 0, 1)
    pane.add(vBox, 1, 1)
    pane.add(right, 2, 1)
    pane.hgap = 16.0
    pane.prefWidth = 600.0
    pane.prefHeight = 400.0

    dialog.dialogPane.content = pane
    dialog.dialogPane.buttonTypes.add(ButtonType.APPLY)

    dialog.resultConverter = Callback { ArrayList(right.items) }
    return dialog.showAndWait()
}
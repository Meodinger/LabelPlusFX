package info.meodinger.lpfx.util.dialog

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Window
import javafx.util.Callback
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
private val dialog: Dialog<ButtonType> = Dialog()

private fun initDialog() {
    dialog.title = ""
    dialog.contentText = ""
    dialog.dialogPane = DialogPane()
}

fun initCDialogOwner(window: Window?) {
    dialog.initOwner(window)
}

/**
 * Show stack trace in expandable content
 * @param e Exception to print
 * @return ButtonType.OK?
 */
fun showException(e: Exception): Optional<ButtonType> {

    // Create expandable Exception.
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    val exceptionText = sw.toString()

    val expContent = GridPane()
    val label = Label("The exception stacktrace is:")
    val textArea = TextArea(exceptionText)
    textArea.isEditable = false
    textArea.prefWidthProperty().bind(expContent.widthProperty())
    textArea.prefHeightProperty().bind(expContent.heightProperty())
    expContent.add(label, 0, 0)
    expContent.add(textArea, 0, 1)
    expContent.prefHeight = 400.0
    expContent.prefWidth = 800.0

    initDialog()
    dialog.title = I18N["common.error"]
    dialog.headerText = e.javaClass.toString()
    dialog.contentText = e.message
    dialog.dialogPane.buttonTypes.add(ButtonType.OK)
    dialog.dialogPane.expandableContent = expContent
    return dialog.showAndWait()
}

/**
 * Show information
 * @param content Info to show
 * @return ButtonType.OK?
 */
fun showInfo(content: String): Optional<ButtonType> {
    return showInfo(I18N["common.info"], content)
}
/**
 * Show information with specific title
 * @param title Dialog title
 * @param content Info to show
 * @return ButtonType.OK?
 */
fun showInfo(title: String, content: String): Optional<ButtonType> {
    initDialog()
    dialog.title = title
    dialog.contentText = content
    dialog.dialogPane.buttonTypes.add(ButtonType.OK)
    return dialog.showAndWait()
}
/**
 * Show information with specific title and link
 * @param title Dialog title
 * @param content Info to show
 * @param linkText Text for hyperlink
 * @param handler Handler for action of link
 * @return ButtonType.OK?
 */
fun showInfoWithLink(title: String, content: String, linkText: String, handler: EventHandler<ActionEvent>): Optional<ButtonType> {
    initDialog()
    dialog.title = title
    dialog.dialogPane.buttonTypes.add(ButtonType.OK)

    val label = Label(content)
    val separator = Separator()
    val hyperlink = Hyperlink(linkText)
    separator.padding = Insets(8.0, 0.0, 8.0, 0.0)
    hyperlink.onAction = handler

    dialog.dialogPane.content = VBox(label, separator, hyperlink)

    return dialog.showAndWait()
}

/**
 * Show alert
 * @param content Alert to show
 * @return ButtonType? with ButtonDate YES | NO
 */
fun showAlert(content: String): Optional<ButtonType> {
    return showAlert(I18N["common.alert"], content)
}
/**
 * Show alert with specific title
 * @param title Dialog title
 * @param content Alert to show
 * @return ButtonType? with ButtonDate YES | NO
 */
fun showAlert(title: String, content: String): Optional<ButtonType> {
    return showAlert(title, content, I18N["common.yes"], I18N["common.no"])
}
/**
 * Show alert with specific title and yes/no text
 * @param title Dialog title
 * @param content Alert to show
 * @param yes Text for ButtonType with ButtonData.YES
 * @param no  Text for ButtonType with ButtonData.NO
 * @return ButtonType? with ButtonDate YES | NO
 */
fun showAlert(title: String, content: String, yes: String, no: String): Optional<ButtonType> {
    initDialog()
    dialog.title = title
    dialog.contentText = content
    dialog.dialogPane.buttonTypes.addAll(
        ButtonType(yes, ButtonBar.ButtonData.YES),
        ButtonType(no, ButtonBar.ButtonData.NO),
        ButtonType(I18N["common.cancel"], ButtonBar.ButtonData.CANCEL_CLOSE)
    )
    return dialog.showAndWait()
}

/**
 * Show message for confirm
 * @param msg Message to show
 * @return ButtonType?.YES | NO
 */
fun showConfirm(msg: String): Optional<ButtonType> {
    return showConfirm(I18N["common.confirm"], msg)
}
/**
 * Show message for confirm with specific title
 * @param title Dialog title
 * @param msg Message to show
 * @return ButtonType?.YES | NO
 */
fun showConfirm(title: String, msg: String): Optional<ButtonType> {
    return showConfirm(title, "", msg)
}
/**
 * Show message for confirm
 * @param title Dialog title
 * @param header Dialog header text
 * @param msg Message to show
 * @return ButtonType?.YES | NO
 */
fun showConfirm(title: String, header: String, msg: String): Optional<ButtonType> {
    initDialog()
    dialog.title = title
    dialog.headerText = header
    dialog.contentText = msg
    dialog.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO)
    return dialog.showAndWait()
}

/**
 * For specific usage
 */

fun <T> showChoice(owner: Window?, title: String, header: String, choices: List<T>): Optional<T> {
    val dialog = ChoiceDialog(choices[0], choices)
    dialog.initOwner(owner)
    dialog.title = title
    dialog.contentText = header
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

fun <T> showListChoose(owner: Window?, list: List<T>): Optional<List<T>> {
    val dialog = Dialog<List<T>>()
    dialog.initOwner(owner)
    dialog.title = I18N["dialog.choose.title"]

    val left = ListView<T>()
    val right = ListView<T>()
    left.selectionModel.selectionMode = SelectionMode.MULTIPLE
    right.selectionModel.selectionMode = SelectionMode.MULTIPLE
    left.items.addAll(list)

    val add = Button(I18N["dialog.choose.add"])
    val addAll = Button(I18N["dialog.choose.add_all"])
    val remove = Button(I18N["dialog.choose.remove"])
    val removeAll = Button(I18N["dialog.choose.remove_all"])
    val vBox = VBox(add, addAll, remove, removeAll)
    vBox.alignment = Pos.CENTER
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
    pane.add(Label(I18N["dialog.choose.potential"]), 0, 0)
    pane.add(Label(I18N["dialog.choose.selected"]), 2, 0)
    pane.add(left, 0, 1)
    pane.add(vBox, 1, 1)
    pane.add(right, 2, 1)
    pane.hgap = 20.0
    pane.prefWidth = 600.0
    pane.prefHeight = 400.0

    dialog.dialogPane.content = pane
    dialog.dialogPane.buttonTypes.add(ButtonType.APPLY)

    dialog.resultConverter = Callback { ArrayList(right.items) }
    return dialog.showAndWait()
}
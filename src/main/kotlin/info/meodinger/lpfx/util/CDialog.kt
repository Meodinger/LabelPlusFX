package info.meodinger.lpfx.util.dialog

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Window
import javafx.util.Callback
import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/8/8
 * Location: info.meodinger.lpfx.util
 */

// Common dialogs
private val confirmDialog = Dialog<ButtonType>()
private val infoDialog = Dialog<ButtonType>()
private val alertDialog = Dialog<ButtonType>()
private val errorDialog = Dialog<ButtonType>()
private val exceptionDialog = Dialog<ButtonType>()

fun initDialogOwner(owner: Window?) {
    confirmDialog.initOwner(owner)
    infoDialog.initOwner(owner)
    alertDialog.initOwner(owner)
    errorDialog.initOwner(owner)
    exceptionDialog.initOwner(owner)
}

/**
 * Show message for confirm
 * @param content Message to show
 * @return ButtonType? YES | NO
 */
fun showConfirm(content: String): Optional<ButtonType> {
    return showConfirm(I18N["common.confirm"], null, content)
}

/**
 * Show message for confirm
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? YES | NO
 */
fun showConfirm(title: String, header: String?, content: String): Optional<ButtonType> {
    return confirmDialog.showAndWait()
}

/**
 * Show information
 * @param content Info to show
 * @return ButtonType? OK
 */
fun showInfo(content: String): Optional<ButtonType> {
    return showInfo(I18N["common.confirm"], null, content)
}
/**
 * Show information
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? OK
 */
fun showInfo(title: String, header: String?, content: String): Optional<ButtonType> {
    return infoDialog.showAndWait()
}

/**
 * Show alert
 * @param content Alert to show
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(content: String): Optional<ButtonType> {
    return showAlert(I18N["common.confirm"], null, content)
}
/**
 * Show alert
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(title: String, header: String?, content: String): Optional<ButtonType> {
    return alertDialog.showAndWait()
}

/**
 * Show error
 * @param content Error to show
 * @return ButtonType? OK
 */
fun showError(content: String): Optional<ButtonType> {
    return showError(I18N["common.confirm"], null, content)
}
/**
 * Show error
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? OK
 */
fun showError(title: String, header: String?, content: String): Optional<ButtonType> {
    return errorDialog.showAndWait()
}

/**
 * Show stack trace in expandable content
 * @param e Exception to print
 * @return ButtonType? OK
 */
fun showException(e: Exception): Optional<ButtonType> {
    return exceptionDialog.showAndWait()
}


// Specific dialogs

fun showLink(owner: Window?, title: String, content: String?, link: String, handler: EventHandler<ActionEvent>): Optional<ButtonType> {
    return Dialog<ButtonType>().showAndWait()
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

fun <T> showChoiceList(owner: Window?, list: List<T>): Optional<List<T>> {
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
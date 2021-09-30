package info.meodinger.lpfx.util.dialog

import info.meodinger.lpfx.options.Logger
import info.meodinger.lpfx.util.image.resizeByRadius
import info.meodinger.lpfx.util.string.omitHighText
import info.meodinger.lpfx.util.string.omitWideText
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.resource.loadImage
import info.meodinger.lpfx.io.LogSender

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Window
import javafx.util.Callback
import java.util.*
import kotlin.collections.ArrayList


/**
 * Author: Meodinger
 * Date: 2021/8/8
 * Location: info.meodinger.lpfx.util
 */

/**
 * Constant
 */
const val DIALOG_ICON_RADIUS = 32.0

// Common dialogs
private val confirmDialog = Dialog<ButtonType>().also {
    it.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO)
    it.graphic = ImageView(loadImage("/image/dialog/Confirm.png").resizeByRadius(DIALOG_ICON_RADIUS))
}
private val infoDialog = Dialog<ButtonType>().also {
    it.dialogPane.buttonTypes.addAll(ButtonType.OK)
    it.graphic = ImageView(loadImage("/image/dialog/Info.png").resizeByRadius(DIALOG_ICON_RADIUS))
}
private val alertDialog = Dialog<ButtonType>().also {
    it.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)
    it.graphic = ImageView(loadImage("/image/dialog/Alert.png").resizeByRadius(DIALOG_ICON_RADIUS))
}
private val errorDialog = Dialog<ButtonType>().also {
    it.dialogPane.buttonTypes.addAll(ButtonType.OK)
    it.graphic = ImageView(loadImage("/image/dialog/Error.png").resizeByRadius(DIALOG_ICON_RADIUS))
}
private val exceptionDialog = Dialog<ButtonType>().also {
    it.isResizable = true
    it.dialogPane.buttonTypes.add(ButtonType.OK)
}

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
    confirmDialog.title = title
    confirmDialog.headerText = header
    confirmDialog.contentText = omitWideText(omitHighText(content), confirmDialog.width / 3 * 2)
    return confirmDialog.showAndWait()
}

/**
 * Show information
 * @param content Info to show
 * @return ButtonType? OK
 */
fun showInfo(content: String): Optional<ButtonType> {
    return showInfo(I18N["common.info"], null, content)
}
/**
 * Show information
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? OK
 */
fun showInfo(title: String, header: String?, content: String): Optional<ButtonType> {
    infoDialog.title = title
    infoDialog.headerText = header
    infoDialog.contentText = omitWideText(omitHighText(content), infoDialog.width / 3 * 2)
    return infoDialog.showAndWait()
}

/**
 * Show alert
 * @param content Alert to show
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(content: String): Optional<ButtonType> {
    return showAlert(I18N["common.alert"], null, content)
}
/**
 * Show alert
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? YES | NO | CANCEL
 */
fun showAlert(title: String, header: String?, content: String): Optional<ButtonType> {
    alertDialog.title = title
    alertDialog.headerText = header
    alertDialog.contentText = omitWideText(omitHighText(content), alertDialog.width / 3 * 2)
    return alertDialog.showAndWait()
}

/**
 * Show error
 * @param content Error to show
 * @return ButtonType? OK
 */
fun showError(content: String): Optional<ButtonType> {
    return showError(I18N["common.error"], null, content)
}
/**
 * Show error
 * @param title Dialog title
 * @param header Header text, nullable
 * @param content Content text
 * @return ButtonType? OK
 */
fun showError(title: String, header: String?, content: String): Optional<ButtonType> {
    errorDialog.title = title
    errorDialog.headerText = header
    errorDialog.contentText = omitWideText(omitHighText(content), errorDialog.width / 3 * 2)
    return errorDialog.showAndWait()
}

/**
 * Show stack trace in expandable content
 * @param e Exception to print
 * @return ButtonType? OK
 */
fun showException(e: Throwable): Optional<ButtonType> {

    // Get exception stack trace
    val text = e.stackTraceToString()

    // Create pane
    val content = VBox(8.0)

    val sentLabel = Label().also {
        it.padding = Insets(0.0, 8.0, 0.0, 0.0)
    }
    val header = HBox(
        Label(e.message ?: e.javaClass.simpleName),
        HBox().also { HBox.setHgrow(it, Priority.ALWAYS) },
        sentLabel,
        Button(I18N["logs.button.send"]).also { it.setOnAction { LogSender.sendLog(Logger.log); sentLabel.text = "Sent" } }
    ).also {
        it.alignment = Pos.CENTER_LEFT
    }
    val textArea = TextArea(text).also {
        it.isEditable = false
        it.prefWidthProperty().bind(content.widthProperty())
        it.prefHeightProperty().bind(content.heightProperty())
    }

    content.children.addAll(header, Separator(), Label("The exception stacktrace is:"), textArea)

    exceptionDialog.title = I18N["common.error"]
    exceptionDialog.headerText = e.javaClass.name
    exceptionDialog.dialogPane.content = content
    exceptionDialog.dialogPane.prefWidth = 600.0
    exceptionDialog.dialogPane.prefHeight = 400.0

    return exceptionDialog.showAndWait()
}

/**
 * Show dialog
 * @param type 0=Confirm, 1=Info, 2=Alert, 3=Error
 */
fun showDialog(owner: Window?, type: Int, title: String, header: String?, content: String): Optional<ButtonType> {
    val dialog = Dialog<ButtonType>()

    dialog.initOwner(owner)
    dialog.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO)
    dialog.title = title
    dialog.headerText = header
    dialog.contentText = content
    when (type) {
        0 -> dialog.graphic = ImageView(loadImage("/image/dialog/Confirm.png").resizeByRadius(DIALOG_ICON_RADIUS))
        1 -> dialog.graphic = ImageView(loadImage("/image/dialog/Info.png").resizeByRadius(DIALOG_ICON_RADIUS))
        2 -> dialog.graphic = ImageView(loadImage("/image/dialog/Alert.png").resizeByRadius(DIALOG_ICON_RADIUS))
        3 -> dialog.graphic = ImageView(loadImage("/image/dialog/Error.png").resizeByRadius(DIALOG_ICON_RADIUS))
    }

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
    dialog.title = I18N["dialog.choose.title"]

    val left = ListView<T>()
    val right = ListView<T>()
    left.selectionModel.selectionMode = SelectionMode.MULTIPLE
    right.selectionModel.selectionMode = SelectionMode.MULTIPLE
    left.items.addAll(unselected)
    right.items.addAll(selected)

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
package ink.meodinger.lpfx.util.dialog

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

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
import java.util.Optional


/**
 * Author: Meodinger
 * Date: 2021/12/7
 * Have fun with my code!
 */

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

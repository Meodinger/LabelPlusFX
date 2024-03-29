package ink.meodinger.lpfx.component.dialog

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.does
import ink.meodinger.lpfx.util.component.withContent

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
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
 * @param graphics Graphics node
 * @param title Dialog title
 * @param header Dialog header
 * @param content Dialog content
 * @param link Link text
 * @param handler Handler for link
 * @return ButtonType? OK | CLOSE
 */
fun showLink(
    owner: Window?,
    graphics: Node?,
    title: String,
    header: String?,
    content: String?,
    link: String,
    handler: EventHandler<ActionEvent>
): Optional<ButtonType> {
    val dialog = Dialog<ButtonType>()
    dialog.initOwner(owner)
    dialog.title = title
    dialog.graphic = graphics
    dialog.headerText = header
    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CLOSE)
    dialog.dialogPane.withContent(VBox()) {
        add(Label(content))
        add(Separator()) { padding = Insets(8.0, 0.0, 8.0, 0.0) }
        add(Hyperlink(link)) { onAction = handler; padding = Insets(0.0) }
    }

    return dialog.showAndWait()
}

/**
 * Show a TextArea
 * @param owner Owner
 * @param title Dialog title
 * @param defaultText Text placeholder
 * @return TextArea text
 */
fun showInputArea(owner: Window?, title: String, defaultText: String): Optional<String> {
    val textArea = TextArea(defaultText)
    val dialog = Dialog<String>()
    dialog.initOwner(owner)
    dialog.title = title
    dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
    dialog.dialogPane withContent HBox(textArea)
    dialog.setResultConverter { if (it == ButtonType.OK) textArea.text else defaultText }

    return dialog.showAndWait()
}

/**
 * Show a dialog that allow user select from a ListView to a ListView
 * @param owner Owner
 * @param unselected Potential items
 * @param selected Already selected items
 * @param cellFactory CellFactory for ListView&lt;T&gt;
 * @return List? of T
 */
fun <T> showChoiceList(owner: Window?, unselected: List<T>, selected: List<T>, cellFactory: Callback<ListView<T>, ListCell<T>>? = null): Optional<List<T>> {
    val left = ListView<T>()
    val right = ListView<T>()

    val mover: (ListView<T>, ListView<T>) -> Unit = { from, to ->
        val items = from.selectionModel.selectedItems
        to.items.addAll(items)
        from.items.removeAll(items)
    }
    val moverAll: (ListView<T>, ListView<T>) -> Unit = { from, to ->
        to.items.addAll(from.items)
        from.items.clear()
    }

    val dialog = Dialog<List<T>>()
    dialog.initOwner(owner)
    dialog.title = I18N["util.dialog.choose.title"]
    dialog.dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
    dialog.dialogPane.withContent(GridPane()) {
        hgap = 16.0
        prefWidth = 600.0
        prefHeight = 400.0
        add(Label(I18N["util.dialog.choose.potential"]), 0, 0)
        add(Label(I18N["util.dialog.choose.selected"]), 2, 0)
        add(left, 0, 1) {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            if (cellFactory != null) setCellFactory(cellFactory)

            items.addAll(unselected)
        }
        add(VBox(), 1, 1) {
            alignment = Pos.CENTER
            add(Button(I18N["util.dialog.choose.add"])) { does { mover(left, right) } }
            add(Button(I18N["util.dialog.choose.add_all"])) { does { moverAll(left, right) } }
            add(Button(I18N["util.dialog.choose.remove"])) { does { mover(right, left) } }
            add(Button(I18N["util.dialog.choose.remove_all"])) { does { moverAll(right, left) } }
            for (node in children) {
                val btn = node as Button
                btn.prefWidth = 48.0
                btn.prefHeight = 48.0
            }
        }
        add(right, 2, 1) {
            selectionModel.selectionMode = SelectionMode.MULTIPLE
            if (cellFactory != null) setCellFactory(cellFactory)

            items.addAll(selected)
        }
    }

    dialog.resultConverter = Callback {
        when(it) {
            ButtonType.CANCEL -> selected
            ButtonType.APPLY -> ArrayList<T>(right.items)
            else -> throw IllegalStateException("Should not reach here")
        }
    }
    return dialog.showAndWait()
}

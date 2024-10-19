package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.property.onChange
import javafx.event.EventHandler
import javafx.scene.control.*

class CTextMenu(
    private val textField: TextInputControl
) : ContextMenu() {

    //    add default menu items
    private val undoMI = MenuItem(I18N["input.menu.undo"]).apply {
        onAction = EventHandler { textField.undo() }
    }
    private val redoMI = MenuItem(I18N["input.menu.redo"]).apply {
        onAction = EventHandler { textField.redo() }
    }
    private val cutMI = MenuItem(I18N["input.menu.cut"]).apply {
        onAction = EventHandler { textField.cut() }
    }
    private val copyMI = MenuItem(I18N["input.menu.copy"]).apply {
        onAction = EventHandler { textField.copy() }
    }
    private val pasteMI = MenuItem(I18N["input.menu.paste"]).apply {
        onAction = EventHandler { textField.paste() }
    }
    private val deleteMI = MenuItem(I18N["input.menu.delete_selection"]).apply {
        onAction = EventHandler { deleteSelectedText(textField) }
    }
    private val selectAllMI = MenuItem(I18N["input.menu.select_all"]).apply {
        onAction = EventHandler { textField.selectAll() }
    }

    //    add custom menu items
    private val quickInput = Menu(I18N["input.menu.quick_input"])


    init {
        textField.undoableProperty()
            .addListener { _, _, newValue ->
                undoMI.isDisable =
                    !newValue!!
            }
        textField.redoableProperty()
            .addListener { _, _, newValue ->
                redoMI.isDisable =
                    !newValue!!
            }
        textField.selectionProperty()
            .addListener { _, _, newValue ->
                cutMI.isDisable =
                    newValue.length == 0
                copyMI.isDisable = newValue.length == 0
                deleteMI.isDisable = newValue.length == 0
                selectAllMI.isDisable = newValue.length == newValue.end
            }

        //init QuickInputItems
        initQuickInputItems(quickInput)
        textField.contextMenu = ContextMenu(
            undoMI, redoMI, cutMI, copyMI, pasteMI, deleteMI, SeparatorMenuItem(), selectAllMI, quickInput
        )
    }

    private fun deleteSelectedText(t: TextInputControl) {
        val range = t.selection
        if (range.length == 0) {
            return
        }
        val text = t.text
        val newText = text.substring(0, range.start) + text.substring(range.end)
        t.text = newText
        t.positionCaret(range.start)
    }

    private fun initQuickInputItems(menu: Menu) {
        menu.items.clear()
        menu.items.addAll(getQuickInputItems(textField))
        Settings.quickInputTextsProperty.addListener( onChange {
            Logger.info("refresh the quick input items", "CTextMenu")
            menu.items.clear()
            menu.items.addAll(getQuickInputItems(textField))
        })
    }

    private fun getQuickInputItems(t: TextInputControl): List<MenuItem> {
        return Settings.quickInputTexts.map {
            MenuItem(it).apply {
                onAction =  EventHandler {  t.appendText(text) }
            }
        }
    }


}

package info.meodinger.lpfx.component

import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.util.tree.expandAll

import javafx.geometry.Pos
import javafx.scene.control.TreeCell

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CTreeCell(menu: CTreeMenu, mode: ViewMode): TreeCell<String>() {

    init {
        alignment = Pos.CENTER_LEFT

        setOnMouseClicked { event ->
            if (treeItem != null) {
                if (treeItem.javaClass == CTreeItem::class) {
                    // Label
                    menu.labelMenu.redirectTo(treeItem)
                    contextMenu = menu.labelMenu
                } else if (treeItem.parent != null) {
                    // Group
                    menu.groupMenu.redirectTo(treeItem)
                    contextMenu = menu.groupMenu
                } else {
                    // Root
                    contextMenu = when(mode) {
                        ViewMode.GroupMode -> {
                            menu.rootMenu.redirectTo(treeItem)
                            menu.rootMenu
                        }
                        ViewMode.IndexMode -> {
                            menu.rootMenu.redirectTo(null)
                            null
                        }
                    }
                    if (event.clickCount > 1) {
                        treeItem.expandAll()
                    }
                }
            } else {
                menu.rootMenu.redirectTo(null)
                menu.groupMenu.redirectTo(null)
                menu.labelMenu.redirectTo(null)
                contextMenu = null
            }
        }
    }

    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty) {
            text = null
            graphic = null
        } else {
            graphic = treeItem.graphic
            text = treeItem.value
        }
    }
}
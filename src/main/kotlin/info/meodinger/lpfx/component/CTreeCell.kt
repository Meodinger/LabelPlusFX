package info.meodinger.lpfx.component

import info.meodinger.lpfx.util.tree.expandAll

import javafx.geometry.Pos
import javafx.scene.control.TreeCell

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CTreeCell: TreeCell<String>() {

    init {
        alignment = Pos.CENTER_LEFT

        setOnMouseClicked { event ->
            if (treeItem != null) {
                if (treeItem is CTreeItem) {
                    // Label
                    CTreeMenu.labelMenu.redirectTo(treeItem)
                    contextMenu = CTreeMenu.labelMenu
                } else if (treeItem.parent != null) {
                    // Group
                    CTreeMenu.groupMenu.redirectTo(treeItem)
                    contextMenu = CTreeMenu.groupMenu
                } else {
                    // Root
                    CTreeMenu.rootMenu.redirectTo(treeItem)
                    contextMenu = CTreeMenu.rootMenu
                    if (event.clickCount > 1) {
                        treeItem.expandAll()
                    }
                }
            } else {
                CTreeMenu.rootMenu.redirectTo(null)
                CTreeMenu.groupMenu.redirectTo(null)
                CTreeMenu.labelMenu.redirectTo(null)
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
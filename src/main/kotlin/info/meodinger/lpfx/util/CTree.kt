package info.meodinger.lpfx.util.tree

import javafx.scene.control.TreeItem

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
val <T> TreeItem<T>.root: TreeItem<T>
    get() {
        var root: TreeItem<T> = this
        while (root.parent != null) root = root.parent
        return root
    }

fun TreeItem<*>.expandAll() {
    if (this.children.size > 0) {
        this.isExpanded = true
        for (i in this.children) {
            i.expandAll()
        }
    }
}
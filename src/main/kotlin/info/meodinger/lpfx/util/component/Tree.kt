package info.meodinger.lpfx.util.component

import javafx.scene.control.TreeItem


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */

/**
 * Returns the root of it
 */
val <T> TreeItem<T>.root: TreeItem<T>
    get() {
        var root: TreeItem<T> = this
        while (root.parent != null) root = root.parent
        return root
    }

/**
 * Expand all items under it
 */
fun TreeItem<*>.expandAll() {
    if (this.children.size > 0) {
        this.isExpanded = true
        for (i in this.children) {
            i.expandAll()
        }
    }
}
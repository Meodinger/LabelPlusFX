package info.meodinger.lpfx.util.tree

import javafx.scene.control.TreeItem

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.util
 */
fun <T> getRootOf(item: TreeItem<T>?): TreeItem<T>? {
    if (item == null) return null

    var root: TreeItem<T> = item
    while (root.parent != null) root = root.parent
    return root
}

fun expandAll(item: TreeItem<*>?) {
    if (item == null) return

    if (item.children.size > 0) {
        item.isExpanded = true
        for (i in item.children) {
            expandAll(i)
        }
    }
}
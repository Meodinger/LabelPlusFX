package ink.meodinger.lpfx.util.component

import javafx.scene.Node
import javafx.scene.control.Dialog
import javafx.scene.control.SplitPane
import javafx.scene.layout.*


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */

////////////////////////////////////////////////////////////
///// AnchorPane Anchor / Layout
////////////////////////////////////////////////////////////

/**
 * Get AnchorPane anchor - Left
 * @return LayoutX if null
 */
var Node.anchorPaneLeft: Double
    get() = AnchorPane.getLeftAnchor(this) ?: layoutX
    set(value) { AnchorPane.setLeftAnchor(this, value) }

/**
 * Get AnchorPane anchor - Top
 * @return LayoutY if null
 */
var Node.anchorPaneTop: Double
    get() = AnchorPane.getTopAnchor(this) ?: layoutY
    set(value) { AnchorPane.setTopAnchor(this, value) }

/**
 * Get AnchorPane anchor - Right
 * @return LayoutX + localBoundsWidth if null
 */
var Node.anchorPaneRight: Double
    get() = AnchorPane.getRightAnchor(this) ?: (layoutX + boundsInLocal.width)
    set(value) { AnchorPane.setLeftAnchor(this, value) }

/**
 * Get AnchorPane anchor - Bottom
 * @return LayoutY + localBoundsHeight if null
 */
var Node.anchorPaneBottom: Double
    get() = AnchorPane.getBottomAnchor(this) ?: (layoutY + boundsInLocal.height)
    set(value) { AnchorPane.setBottomAnchor(this, value) }

////////////////////////////////////////////////////////////
///// Pane/DialogPane content
////////////////////////////////////////////////////////////

/**
 * Set pane.children
 * @param content Content node of Pane
 * @param operation Lambda with arguments of Node as this and Pane as it
 * @return this pane ref
 */
fun <T : Node> Pane.withContent(content: T, operation: T.() -> Unit): Pane {
    return apply {
        children.clear()
        children.add(content.also { operation(it) })
    }
}
infix fun <T : Node> Pane.withContent(content: T): Pane {
    return apply {
        children.clear()
        children.add(content)
    }
}

/**
 * Set dialogPane.content
 * @param content Content node of DialogPane
 * @param operation Lambda with arguments of Node as this and Dialog as it
 * @return this dialog ref
 */
fun <T : Node, R> Dialog<R>.withContent(content: T, operation: T.() -> Unit): Dialog<R> {
    return apply { dialogPane.content = content.also(operation) }
}
infix fun <T : Node, R> Dialog<R>.withContent(content: T) : Dialog<R> {
    return apply { dialogPane.content = content }
}

////////////////////////////////////////////////////////////
///// BorderPane
////////////////////////////////////////////////////////////

/**
 * Apply a node to BorderPane center
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.center(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { center = node.also(operation) }
}
fun <T : Node> BorderPane.top(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { top = node.also(operation) }
}
fun <T : Node> BorderPane.bottom(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { bottom = node.also(operation) }
}
fun <T : Node> BorderPane.left(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { left = node.also(operation) }
}
fun <T : Node> BorderPane.right(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { right = node.also(operation) }
}

////////////////////////////////////////////////////////////
///// SplitPane
////////////////////////////////////////////////////////////

fun <T : Node> SplitPane.add(node: T, operation: T.() -> Unit = {}): SplitPane {
    return apply { items.add(node.also(operation)) }
}

////////////////////////////////////////////////////////////
///// GridPane
////////////////////////////////////////////////////////////

fun <T : Node> GridPane.add(node: T, col: Int, row: Int, colSpan: Int, rowSpan: Int, operation: T.() -> Unit = {}): GridPane {
    return apply { add(node.also(operation), col, row, colSpan, rowSpan) }
}
fun <T : Node> GridPane.add(node: T, col: Int, row: Int, operation: T.() -> Unit = {}): GridPane {
    return add(node, col, row, 1, 1, operation)
}

////////////////////////////////////////////////////////////
///// HBox / VBox
////////////////////////////////////////////////////////////

fun <T : Node> HBox.add(node: T, operation: T.() -> Unit = {}): HBox {
    return apply { children.add(node.also(operation)) }
}
fun <T : Node> VBox.add(node: T, operation: T.() -> Unit = {}): VBox {
    return apply { children.add(node.also(operation)) }
}

var Node.hGrow: Priority
    get() = HBox.getHgrow(this) ?: Priority.NEVER
    set(value) { HBox.setHgrow(this, value) }

var Node.vGrow: Priority
    get() = VBox.getVgrow(this) ?: Priority.NEVER
    set(value) { VBox.setVgrow(this, value) }

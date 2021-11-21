package ink.meodinger.lpfx.util.component

import javafx.scene.Node
import javafx.scene.control.Dialog
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane

/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */

/**
 * Get AnchorPane anchor - Left
 * @return LayoutX if null
 */
var Node.anchorPaneLeft: Double
    get() =  AnchorPane.getLeftAnchor(this) ?: layoutX
    set(value) { AnchorPane.setLeftAnchor(this, value) }

/**
 * Get AnchorPane anchor - Top
 * @return LayoutY if null
 */
var Node.anchorPaneTop: Double
    get() =  AnchorPane.getTopAnchor(this) ?: layoutY
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
    get() =  AnchorPane.getBottomAnchor(this) ?: (layoutY + boundsInLocal.height)
    set(value) { AnchorPane.setBottomAnchor(this, value) }

/**
 * Set pane.children
 * @param content Content node of Pane
 * @param operation Lambda with arguments of Node as this and Pane as it
 * @return this pane ref
 */
fun <T : Node> Pane.withContent(content: T, operation: T.(Pane) -> Unit): Pane {
    this.children.clear()
    this.children.add(content.apply { operation.invoke(this, this@withContent) })
    return this
}
infix fun <T : Node> Pane.withContent(content: T): Pane {
    this.children.clear()
    this.children.add(content)
    return this
}

/**
 * Set dialogPane.content
 * @param content Content node of DialogPane
 * @param operation Lambda with arguments of Node as this and Dialog as it
 * @return this dialog ref
 */
fun <T : Node, R> Dialog<R>.withContent(content: T, operation: T.(Dialog<R>) -> Unit): Dialog<R> {
    this.dialogPane.content = content.apply { operation.invoke(this, this@withContent) }
    return this
}
infix fun <T : Node, R> Dialog<R>.withContent(content: T) : Dialog<R> {
    this.dialogPane.content = content
    return this
}

/**
 * Apply a node to BorderPane center
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.center(node : T, operation: T.(BorderPane) -> Unit): BorderPane {
    return this.apply { this.center = node.also { operation(it, this) } }
}
fun <T : Node> BorderPane.center(node: T): BorderPane {
    return this.apply { this.center = node }
}

/**
 * Apply a node to BorderPane top
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.top(node : T, operation: T.(BorderPane) -> Unit): BorderPane {
    return this.apply { this.top = node.also { operation(it, this) } }
}
fun <T : Node> BorderPane.top(node: T): BorderPane {
    return this.apply { this.top = node }
}

/**
 * Apply a node to BorderPane bottom
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.bottom(node : T, operation: T.(BorderPane) -> Unit): BorderPane {
    return this.apply { this.bottom = node.also { operation(it, this) } }
}
fun <T : Node> BorderPane.bottom(node: T): BorderPane {
    return this.apply { this.bottom = node }
}

/**
 * Apply a node to BorderPane left
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.left(node : T, operation: T.(BorderPane) -> Unit): BorderPane {
    return this.apply { this.left = node.also { operation(it, this) } }
}
fun <T : Node> BorderPane.left(node: T): BorderPane {
    return this.apply { this.left = node }
}

/**
 * Apply a node to BorderPane right
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.right(node : T, operation: T.(BorderPane) -> Unit): BorderPane {
    return this.apply { this.right = node.also { operation(it, this) } }
}
fun <T : Node> BorderPane.right(node: T): BorderPane {
    return this.apply { this.right = node }
}

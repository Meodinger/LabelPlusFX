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

infix fun Pane.withContent(content: Node): Pane {
    this.children.add(content)
    return this
}

infix fun Dialog<*>.withContent(content: Node) : Dialog<*> {
    this.dialogPane.content = content
    return this
}

/**
 * Apply a node to BorderPane center
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.center(node : T, operation: T.() -> Unit): BorderPane {
    return this.apply { this.center = node.apply(operation) }
}

/**
 * Apply a node to BorderPane top
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.top(node : T, operation: T.() -> Unit): BorderPane {
    return this.apply { this.top = node.apply(operation) }
}

/**
 * Apply a node to BorderPane bottom
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.bottom(node : T, operation: T.() -> Unit): BorderPane {
    return this.apply { this.bottom = node.apply(operation) }
}

/**
 * Apply a node to BorderPane left
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.left(node : T, operation: T.() -> Unit): BorderPane {
    return this.apply { this.left = node.apply(operation) }
}

/**
 * Apply a node to BorderPane right
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.right(node : T, operation: T.() -> Unit): BorderPane {
    return this.apply { this.right = node.apply(operation) }
}
package ink.meodinger.lpfx.util.component

import javafx.beans.value.ObservableValue
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */


////////////////////////////////////////////////////////////
///// Dialog / DialogPane
////////////////////////////////////////////////////////////

fun <T : Node> DialogPane.withContent(content: T, operation: T.() -> Unit = {}): DialogPane {
    return apply { this.content = content.apply(operation) }
}
infix fun <T : Node> DialogPane.withContent(content: T) : DialogPane {
    return apply { this.content = content }
}

/**
 * Add dialogPane.content
 * @param content Content node of DialogPane
 * @param operation Lambda with arguments of Node as this and Dialog as it
 * @return this dialog ref
 */
fun <T : Node, R> Dialog<R>.withContent(content: T, operation: T.() -> Unit = {}): Dialog<R> {
    return apply { dialogPane.withContent(content, operation) }
}
infix fun <T : Node, R> Dialog<R>.withContent(content: T) : Dialog<R> {
    return apply { dialogPane.withContent(content) }
}

////////////////////////////////////////////////////////////
///// Pane content
////////////////////////////////////////////////////////////

/**
 * Add pane#children
 * @param node Node of Pane
 * @param operation Lambda with arguments of Node as this
 * @return this pane ref
 */
fun <T : Node> Pane.add(node: T, operation: T.() -> Unit = {}): Pane {
    return apply { children.add(node.apply(operation)) }
}

fun <T : Node> Pane.withContent(content: T, operation: T.() -> Unit = {}): Pane {
    return apply {
        children.clear()
        add(content, operation)
    }
}
infix fun <T : Node> Pane.withContent(content: T): Pane {
    return withContent(content) {}
}

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
///// BorderPane
////////////////////////////////////////////////////////////

/**
 * Apply a node to BorderPane center
 * @param node Node to apply
 * @param operation Operation to node (will translate to node.apply(operation))
 * @return this BorderPane
 */
fun <T : Node> BorderPane.center(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { center = node.apply(operation) }
}
fun <T : Node> BorderPane.top(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { top = node.apply(operation) }
}
fun <T : Node> BorderPane.bottom(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { bottom = node.apply(operation) }
}
fun <T : Node> BorderPane.left(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { left = node.apply(operation) }
}
fun <T : Node> BorderPane.right(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { right = node.apply(operation) }
}

////////////////////////////////////////////////////////////
///// SplitPane
////////////////////////////////////////////////////////////

fun <T : Node> SplitPane.add(node: T, operation: T.() -> Unit = {}): SplitPane {
    return apply { items.add(node.apply(operation)) }
}

////////////////////////////////////////////////////////////
///// GridPane
////////////////////////////////////////////////////////////

fun <T : Node> GridPane.add(node: T, col: Int, row: Int, colSpan: Int, rowSpan: Int, operation: T.() -> Unit = {}): GridPane {
    return apply { add(node.apply(operation), col, row, colSpan, rowSpan) }
}
fun <T : Node> GridPane.add(node: T, col: Int, row: Int, operation: T.() -> Unit = {}): GridPane {
    return add(node, col, row, 1, 1, operation)
}

var Node.gridHGrow: Priority
    get() = GridPane.getHgrow(this) ?: Priority.NEVER
    set(value) { GridPane.setHgrow(this, value) }

var Node.gridVGrow: Priority
    get() = GridPane.getVgrow(this) ?: Priority.NEVER
    set(value) { GridPane.setVgrow(this, value) }

var Node.gridHAlign: HPos
    get() = GridPane.getHalignment(this) ?: HPos.LEFT
    set(value) { GridPane.setHalignment(this, value) }

var Node.gridVAlign: VPos
    get() = GridPane.getValignment(this) ?: VPos.BASELINE
    set(value) { GridPane.setValignment(this, value) }

////////////////////////////////////////////////////////////
///// ScrollPane
////////////////////////////////////////////////////////////

fun <T : Node> ScrollPane.withContent(content: T, operation: T.() -> Unit = {}): ScrollPane {
    return apply { this.content = content.apply(operation) }
}
infix fun <T : Node> ScrollPane.withContent(content: T): ScrollPane {
    return withContent(content) {}
}

////////////////////////////////////////////////////////////
///// TabPane
////////////////////////////////////////////////////////////

fun TabPane.add(tab: Tab, operation: Tab.() -> Unit = {}): TabPane {
    return apply { tabs.add(tab.apply(operation)) }
}
fun TabPane.add(title: String, operation: Tab.() -> Unit = {}): TabPane {
    return apply { add(Tab(title), operation) }
}

fun <T : Node> Tab.withContent(node: T, operation: T.() -> Unit): Tab {
    return apply { content = node.apply(operation) }
}
infix fun <T : Node> Tab.withContent(node: T): Tab {
    return withContent(node) {}
}

////////////////////////////////////////////////////////////
///// HBox / VBox
////////////////////////////////////////////////////////////

fun <T : Node> HBox.add(node: T, operation: T.() -> Unit = {}): HBox {
    return apply { children.add(node.apply(operation)) }
}
fun <T : Node> VBox.add(node: T, operation: T.() -> Unit = {}): VBox {
    return apply { children.add(node.apply(operation)) }
}

fun HBox.addAll(vararg nodes: Node): HBox {
    return apply { children.addAll(*nodes) }
}
fun VBox.addAll(vararg nodes: Node): VBox {
    return apply { children.addAll(*nodes) }
}

var Node.boxHGrow: Priority
    get() = HBox.getHgrow(this) ?: Priority.NEVER
    set(value) { HBox.setHgrow(this, value) }

var Node.boxVGrow: Priority
    get() = VBox.getVgrow(this) ?: Priority.NEVER
    set(value) { VBox.setVgrow(this, value) }

////////////////////////////////////////////////////////////
///// Table View
////////////////////////////////////////////////////////////

fun <S, T> TableView<S>.addColumn(title: String, getter: (TableColumn.CellDataFeatures<S, T>) -> ObservableValue<T>): TableView<S> {
    return apply { columns.add(TableColumn<S, T>(title).apply { setCellValueFactory(getter) }) }
}

////////////////////////////////////////////////////////////
///// Menu/MenuBar
////////////////////////////////////////////////////////////

fun MenuBar.menu(menu: Menu, operation: Menu.() -> Unit): MenuBar {
    return apply { menus.add(menu.apply(operation)) }
}
fun MenuBar.menu(text: String, operation: Menu.() -> Unit): MenuBar {
    return apply { menu(Menu(text), operation) }
}

fun <T : MenuItem> Menu.add(item: T, operation: T.() -> Unit = {}): Menu {
    return apply { items.add(item.apply(operation)) }
}
fun Menu.item(item: MenuItem, operation: MenuItem.() -> Unit = {}): Menu {
    return apply { add(item, operation) }
}
fun Menu.item(text: String, operation: MenuItem.() -> Unit = {}): Menu {
    return apply { item(MenuItem(text), operation) }
}
fun Menu.menu(menu: Menu, operation: Menu.() -> Unit = {}): Menu {
    return apply { add(menu, operation) }
}
fun Menu.menu(text: String, operation: Menu.() -> Unit = {}): Menu {
    return apply { menu(Menu(text), operation) }
}
fun Menu.separator(): Menu {
    return apply { add(SeparatorMenuItem()) }
}

////////////////////////////////////////////////////////////
///// Font
////////////////////////////////////////////////////////////

fun Font.f(family: String): Font = Font.font(family, size)
fun Font.s(size: Double): Font = Font.font(family, size)
fun Font.bold(): Font = Font.font(family, FontWeight.BOLD, size)

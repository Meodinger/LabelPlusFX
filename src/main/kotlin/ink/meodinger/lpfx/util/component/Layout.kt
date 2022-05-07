@file:Suppress("unused", "KDocMissingDocumentation")

package ink.meodinger.lpfx.util.component

import javafx.beans.value.ObservableValue
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import javafx.stage.Window


/**
 * Author: Meodinger
 * Date: 2021/9/30
 * Have fun with my code!
 */

////////////////////////////////////////////////////////////
///// Stage
////////////////////////////////////////////////////////////

infix fun <T : Stage> T.withOwner(owner: Window?): T {
    return apply { initOwner(owner) }
}

////////////////////////////////////////////////////////////
///// Dialog / DialogPane
////////////////////////////////////////////////////////////

inline fun <T : Node> DialogPane.withContent(node: T, operation: T.() -> Unit): DialogPane {
    return apply { content = node.apply(operation) }
}
infix fun <T : Node> DialogPane.withContent(node: T) : DialogPane {
    return apply { content = node }
}

infix fun <T : Dialog<*>> T.withOwner(owner: Window?): T {
    return apply { initOwner(owner) }
}

////////////////////////////////////////////////////////////
///// Pane content
////////////////////////////////////////////////////////////

inline fun <T : Node> Pane.add(node: T, operation: T.() -> Unit = {}): Pane {
    return apply { children.add(node.apply(operation)) }
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
///// StackPane
////////////////////////////////////////////////////////////

/**
 * Set content node of a StackPane. Note that StackPane could have many children
 * but this function means to make StackPane has only one child as its content node.
 * @param content Content node of StackPane
 * @param operation Lambda with arguments of Node as this
 * @return this StackPane reference
 */
inline fun <T : Node> StackPane.withContent(content: T, operation: T.() -> Unit): StackPane {
    return apply {
        children.clear()
        children.add(content.apply(operation))
    }
}
/**
 * @see StackPane.withContent
 */
infix fun <T : Node> StackPane.withContent(content: T): StackPane {
    return apply {
        children.clear()
        children.add(content)
    }
}

////////////////////////////////////////////////////////////
///// BorderPane
////////////////////////////////////////////////////////////

inline fun <T : Node> BorderPane.center(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { center = node.apply(operation) }
}
inline fun <T : Node> BorderPane.top(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { top = node.apply(operation) }
}
inline fun <T : Node> BorderPane.bottom(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { bottom = node.apply(operation) }
}
inline fun <T : Node> BorderPane.left(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { left = node.apply(operation) }
}
inline fun <T : Node> BorderPane.right(node : T, operation: T.() -> Unit = {}): BorderPane {
    return apply { right = node.apply(operation) }
}

////////////////////////////////////////////////////////////
///// SplitPane
////////////////////////////////////////////////////////////

inline fun <T : Node> SplitPane.add(node: T, operation: T.() -> Unit = {}): SplitPane {
    return apply { items.add(node.apply(operation)) }
}

////////////////////////////////////////////////////////////
///// GridPane
////////////////////////////////////////////////////////////

// Maybe: Take back `COMMON_GAP = 16.0` as `GENERAL_GRID_GAP`

inline fun <T : Node> GridPane.add(node: T, col: Int, row: Int, colSpan: Int, rowSpan: Int, operation: T.() -> Unit = {}): GridPane {
    return apply { add(node.apply(operation), col, row, colSpan, rowSpan) }
}
inline fun <T : Node> GridPane.add(node: T, col: Int, row: Int, operation: T.() -> Unit = {}): GridPane {
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

inline fun <T : Node> ScrollPane.withContent(node: T, operation: T.() -> Unit): ScrollPane {
    return apply { content = node.apply(operation) }
}
infix fun <T : Node> ScrollPane.withContent(node: T): ScrollPane {
    return apply { content = node }
}

////////////////////////////////////////////////////////////
///// TitledPane
////////////////////////////////////////////////////////////

inline fun <T : Node> TitledPane.withContent(node: T, operation: T.() -> Unit): TitledPane {
    return apply { content = node.apply(operation) }
}
infix fun <T : Node> TitledPane.withContent(node: T): TitledPane {
    return apply { content = node }
}

////////////////////////////////////////////////////////////
///// TabPane
////////////////////////////////////////////////////////////

inline fun TabPane.add(tab: Tab, operation: Tab.() -> Unit = {}): TabPane {
    return apply { tabs.add(tab.apply(operation)) }
}
inline fun TabPane.add(title: String, operation: Tab.() -> Unit = {}): TabPane {
    return apply { add(Tab(title), operation) }
}

inline fun <T : Node> Tab.withContent(node: T, operation: T.() -> Unit): Tab {
    return apply { content = node.apply(operation) }
}
infix fun <T : Node> Tab.withContent(node: T): Tab {
    return apply { content = node }
}

////////////////////////////////////////////////////////////
///// HBox / VBox
////////////////////////////////////////////////////////////

inline fun <T : Node> HBox.add(node: T, operation: T.() -> Unit = {}): HBox {
    return apply { children.add(node.apply(operation)) }
}
inline fun <T : Node> VBox.add(node: T, operation: T.() -> Unit = {}): VBox {
    return apply { children.add(node.apply(operation)) }
}

/**
 * Alias for HBox.setHGrow
 */
var Node.hgrow: Priority
    get() = HBox.getHgrow(this) ?: Priority.NEVER
    set(value) { HBox.setHgrow(this, value) }

/**
 * Alias for VBox.setVGrow
 */
var Node.vgrow: Priority
    get() = VBox.getVgrow(this) ?: Priority.NEVER
    set(value) { VBox.setVgrow(this, value) }

////////////////////////////////////////////////////////////
///// Table View
////////////////////////////////////////////////////////////

// inline inavaiable
fun <S, T> TableView<S>.addColumn(title: String, getter: (TableColumn.CellDataFeatures<S, T>) -> ObservableValue<T>): TableView<S> {
    return apply { columns.add(TableColumn<S, T>(title).apply { setCellValueFactory(getter) }) }
}

////////////////////////////////////////////////////////////
///// Menu/MenuBar
////////////////////////////////////////////////////////////

inline fun MenuBar.menu(menu: Menu, operation: Menu.() -> Unit): MenuBar {
    return apply { menus.add(menu.apply(operation)) }
}
inline fun MenuBar.menu(text: String, operation: Menu.() -> Unit): MenuBar {
    return apply { menu(Menu(text), operation) }
}

inline fun <T : MenuItem> Menu.add(item: T, operation: T.() -> Unit = {}): Menu {
    return apply { items.add(item.apply(operation)) }
}
inline fun Menu.item(item: MenuItem, operation: MenuItem.() -> Unit = {}): Menu {
    return apply { add(item, operation) }
}
inline fun Menu.item(text: String, operation: MenuItem.() -> Unit = {}): Menu {
    return apply { item(MenuItem(text), operation) }
}
inline fun Menu.checkItem(text: String, selected: Boolean, operation: CheckMenuItem.() -> Unit = {}): Menu {
    return apply { add(CheckMenuItem(text)) { isSelected = selected; operation(this) } }
}
inline fun Menu.checkItem(text: String, operation: CheckMenuItem.() -> Unit = {}): Menu {
    return apply { checkItem(text, false, operation) }
}
inline fun Menu.menu(menu: Menu, operation: Menu.() -> Unit = {}): Menu {
    return apply { add(menu, operation) }
}
inline fun Menu.menu(text: String, operation: Menu.() -> Unit = {}): Menu {
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

package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.component.bold
import ink.meodinger.lpfx.util.component.s
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.onNew

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Text
import javafx.scene.text.TextFlow

/**
 * Author: Meodinger
 * Date: 2022/3/31
 * Have fun with my code!
 */

/**
 * A TextFlow provides lazy layout
 */
class CTextFlow : TextFlow() {

    private val nodesProperty: ListProperty<Node> = SimpleListProperty(FXCollections.observableArrayList())
    /**
     * The nodes to be added, note that before invoking `flow()` these nodes
     * will not be added to the TextFlow
     */
    fun nodesProperty(): ListProperty<Node> = nodesProperty
    /**
     * @see nodesProperty
     */
    var nodes: ObservableList<Node> by nodesProperty

    private val fontSizeProperty: DoubleProperty = SimpleDoubleProperty(12.0)
    /**
     * The default font size for the Text in the TextFlow
     */
    fun fontSizeProperty(): DoubleProperty = fontSizeProperty
    /**
     * @see fontSizeProperty
     */
    var fontSize: Double by fontSizeProperty

    private val textColorProperty: ObjectProperty<Paint> = SimpleObjectProperty(Color.BLACK)
    /**
     * The default font color for the Text in the TextFlow
     */
    fun textColorProperty(): ObjectProperty<Paint> = textColorProperty
    /**
     * @see textColorProperty
     */
    var textColor: Paint by textColorProperty

    private val instantProperty: BooleanProperty = SimpleBooleanProperty(true)
    /**
     * Whether update the flow after nodes changed instantly.
     */
    fun instantProperty(): BooleanProperty = instantProperty
    /**
     * @see instantProperty
     */
    var isInstant: Boolean by instantProperty

    init {
        val listener = ListChangeListener<Node> { flow() }
        instantProperty.addListener(onNew {
            if (it) nodes.addListener(listener) else nodes.removeListener(listener)
        })
    }

    /**
     * Update the text flow
     */
    fun flow() {
        children.setAll(nodes)
    }

    /**
     * Append some text to the TextFlow
     */
    fun appendText(text: String, size: Double = fontSize, bold: Boolean = false, color: Paint = textColor) {
        nodes.add(Text(text).apply {
            fill = color
            font = font.s(size).let { if (bold) it.bold() else it }
        })
    }

    /**
     * Append some text and an EOL to the TextFlow
     */
    fun appendLine(text: String, size: Double = fontSize, bold: Boolean = false, color: Paint = textColor) {
        appendText(text.plus("\n"), size, bold, color)
    }

    /**
     * Append  an EOL to the TextFlow
     */
    fun appendLine() {
        appendText("\n")
    }

    /**
     * Cleat all texts and append some text
     */
    fun setText(text: String, size: Double = fontSize, bold: Boolean = false, color: Paint = textColor) {
        nodes.clear()
        appendText(text, size, bold, color)
    }

    /**
     * Clear all texts
     */
    fun clear() {
        nodes.clear()
    }

}

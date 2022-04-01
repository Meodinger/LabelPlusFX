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
 * Double-click to show TextArea
 */
class CTextFlow : TextFlow() {

    private val nodesProperty: ListProperty<Node> = SimpleListProperty(FXCollections.observableArrayList())
    fun nodesProperty(): ListProperty<Node> = nodesProperty
    var nodes: ObservableList<Node> by nodesProperty

    private val fontSizeProperty: DoubleProperty = SimpleDoubleProperty(12.0)
    fun fontSizeProperty(): DoubleProperty = fontSizeProperty
    var fontSize: Double by fontSizeProperty

    private val textColorProperty: ObjectProperty<Paint> = SimpleObjectProperty(Color.BLACK)
    fun textColorProperty(): ObjectProperty<Paint> = textColorProperty
    var textColor: Paint by textColorProperty

    private val instantProperty: BooleanProperty = SimpleBooleanProperty(true)
    fun instantProperty(): BooleanProperty = instantProperty
    /**
     * Whether update the flow after nodes changed instantly.
     */
    var isInstant: Boolean by instantProperty

    private val listener = ListChangeListener<Node> { flow() }

    init {
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

    fun appendText(text: String, size: Double = fontSize, bold: Boolean = false, color: Paint = textColor) {
        nodes.add(Text(text).apply {
            fill = color
            font = font.s(size).let { if (bold) it.bold() else it }
        })
    }
    fun appendLine(text: String, size: Double = fontSize, bold: Boolean = false, color: Paint = textColor) {
        appendText(text.plus("\n"), size, bold, color)
    }
    fun appendLine() {
        appendText("\n")
    }

    fun setText(text: String, size: Double = fontSize, bold: Boolean = false, color: Paint = textColor) {
        nodes.clear()
        appendText(text, size, bold, color)
    }

    fun clear() {
        nodes.clear()
    }

}

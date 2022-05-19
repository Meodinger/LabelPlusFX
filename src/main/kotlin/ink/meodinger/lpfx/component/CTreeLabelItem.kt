package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.type.TransLabel
import ink.meodinger.lpfx.util.property.transform
import ink.meodinger.lpfx.util.string.pad
import ink.meodinger.lpfx.util.string.replaceEOL

import javafx.beans.binding.Bindings
import javafx.scene.control.TreeItem
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A TreeItem for TransLabel containing
 * @param transLabel Associated TransLabel
 */
class CTreeLabelItem(val transLabel: TransLabel, showGraphics: Boolean = false) : TreeItem<String>() {

    companion object {
        /**
         * TreeItem Graphic radius
         */
        const val GRAPHICS_CIRCLE_RADIUS: Double = 8.0
    }

    init {
        valueProperty().bind(Bindings.createStringBinding(
            { "${transLabel.index.pad(2)}: ${transLabel.text.replaceEOL(" ")}" },
            transLabel.indexProperty(), transLabel.textProperty()
        ))
        if (showGraphics) {
            graphicProperty().bind(transLabel.colorProperty().transform { Circle(GRAPHICS_CIRCLE_RADIUS, it) })
        }
    }

}

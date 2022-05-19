package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.type.TransGroup
import ink.meodinger.lpfx.util.property.transform

import javafx.scene.control.TreeItem
import javafx.scene.shape.Circle


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A TreeItem for TransGroup containing
 * @param transGroup Associated TransGroup
 */
class CTreeGroupItem(val transGroup: TransGroup) : TreeItem<String>() {

    companion object {
        /**
         * TreeItem Graphic radius
         */
        const val GRAPHICS_CIRCLE_RADIUS: Double = 8.0
    }

    init {
        valueProperty().bind(transGroup.nameProperty())
        graphicProperty().bind(transGroup.colorProperty().transform { Circle(GRAPHICS_CIRCLE_RADIUS, it) })
    }

}

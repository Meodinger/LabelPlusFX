package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.anchorB
import ink.meodinger.lpfx.util.component.anchorR
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.transform

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

/**
 * Author: Meodinger
 * Date: 2022/5/20
 * Have fun with my code!
 */

/**
 * ResizeGrid is a component windows supports.
 * @see <a href="https://docs.microsoft.com/en-us/dotnet/api/system.windows.controls.primitives.resizegrip?view=windowsdesktop-6.0">ResizeGrid</a>
 */
class CResizeGrid : Control() {

    companion object {
        /**
         * North-West <-> South-East
         */
        const val NW_SE: Int = 0

        /**
         * North <-> South
         */
        const val N_S  : Int = 1

        /**
         * North-East <-> South-West
         */
        const val NE_SW: Int = 2

        /**
         * North <-> South
         */
        const val W_E  : Int = 3
    }

    private val owner by lazy { this.scene.window }

    private val typeProperty: IntegerProperty = SimpleIntegerProperty(NW_SE)
    /**
     * Indicate the cursor when hover
     */
    fun typeProperty(): IntegerProperty = typeProperty
    /**
     * @see typeProperty
     */
    var type: Int by typeProperty

    /**
     * Create default Skin
     * @see javafx.scene.control.Control.createDefaultSkin
     */
    override fun createDefaultSkin(): Skin<CResizeGrid> = ResizeGridSkin(this)

    private class ResizeGridSkin(private val control: CResizeGrid): Skin<CResizeGrid> {

        companion object {
            private const val SIZE: Double = 2.4
            private const val GAP: Double = 0.8
        }

        private val root = AnchorPane()

        private var startX = 0.0
        private var startY = 0.0
        private var startW = 0.0
        private var startH = 0.0

        init {
            root.cursorProperty().bind(control.typeProperty().transform {
                when (it) {
                    0 -> Cursor.NW_RESIZE
                    1 -> Cursor.N_RESIZE
                    2 -> Cursor.NE_RESIZE
                    3 -> Cursor.E_RESIZE
                    else -> throw IllegalStateException()
                }
            })

            root.addEventHandler(MouseEvent.MOUSE_PRESSED) {
                startX = it.screenX
                startY = it.screenY
                startW = control.owner.width
                startH = control.owner.height
            }
            root.addEventHandler(MouseEvent.MOUSE_DRAGGED) {
                control.owner.width  = startW + (it.screenX - startX)
                control.owner.height = startH + (it.screenY - startY)
            }

            root.apply {
                val color = Color.rgb(191,191,191) // 0xBFBFBF
                fun createGreyRect(x: Int, y: Int): Rectangle {
                    return Rectangle(SIZE, SIZE, color).apply {
                        anchorR = x * (SIZE + GAP) - SIZE
                        anchorB = y * (SIZE + GAP) - SIZE
                    }
                }
                //     6
                //   5 4 ↑
                // 3 2 1 y
                //   ← x 0
                add(createGreyRect(1, 1))
                add(createGreyRect(2, 1))
                add(createGreyRect(3, 1))
                add(createGreyRect(1, 2))
                add(createGreyRect(2, 2))
                add(createGreyRect(1, 3))
            }
        }

        override fun getSkinnable(): CResizeGrid = control

        override fun getNode(): Node = root

        override fun dispose() {
            root.cursorProperty().unbind()
        }

    }

}

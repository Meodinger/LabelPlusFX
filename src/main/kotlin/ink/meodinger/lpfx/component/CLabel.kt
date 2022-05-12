package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.Config.MonoFont
import ink.meodinger.lpfx.util.color.opacity
import ink.meodinger.lpfx.util.property.*

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A Label component for LabelPane
 */
class CLabel(
    labelIndex:  Int    = -1,
    labelRadius: Double = 24.0,
    labelColor:  Color  = Color.web("66CCFF"),
) : Control() {

    companion object {
        /**
         * The minimal pick radius for CLabel
         */
        const val MIN_PICK_RADIUS: Double = 16.0
    }

    // region Properties

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(labelIndex)
    /**
     * The index to display
     */
    fun indexProperty(): IntegerProperty = indexProperty
    /**
     * @see indexProperty
     */
    var index: Int by indexProperty

    private val radiusProperty: DoubleProperty = SimpleDoubleProperty(labelRadius)
    /**
     * The radius of the CLabel
     */
    fun radiusProperty(): DoubleProperty = radiusProperty
    /**
     * @see radiusProperty
     */
    var radius: Double by radiusProperty

    private val textOpaqueProperty: BooleanProperty = SimpleBooleanProperty(false)
    /**
     * Whether the text of the CLabel could be opaque
     */
    fun textOpaqueProperty(): BooleanProperty = textOpaqueProperty
    /**
     * @see textOpaqueProperty
     */
    var isTextOpaque: Boolean by textOpaqueProperty

    private val colorOpacityProperty: DoubleProperty = SimpleDoubleProperty(1.0)
    /**
     * The opacity of the CLabel
     */
    fun colorOpacityProperty(): DoubleProperty = colorOpacityProperty
    /**
     * @see colorOpacityProperty
     */
    var colorOpacity: Double by colorOpacityProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(labelColor)
    /**
     * The color of the CLabel
     */
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    /**
     * @see colorProperty
     */
    var color: Color by colorProperty

    // endregion

    // region Skin

    /**
     * Create default Skin
     * @see javafx.scene.control.Control.createDefaultSkin
     */
    override fun createDefaultSkin(): Skin<CLabel> = CLabelSkin(this)

    private class CLabelSkin(private val control: CLabel) : Skin<CLabel> {

        private val root = Pane()
        private val text = Text()
        private val circle = Circle()

        private var clip: Shape = circle // just a placeholder to make type non-null

        init {
            val pickerRadiusBinding = control.radiusProperty.transform { it.coerceAtLeast(MIN_PICK_RADIUS) }.primitive()

            root.apply root@{
                prefWidthProperty().bind(pickerRadiusBinding * 2)
                prefHeightProperty().bind(pickerRadiusBinding * 2)
            }

            circle.apply {
                radiusProperty().bind(control.radiusProperty)
                centerXProperty().bind(pickerRadiusBinding)
                centerYProperty().bind(pickerRadiusBinding)
            }
            text.apply {
                textOrigin = VPos.CENTER

                textProperty().bind(control.indexProperty.asString())
                fillProperty().bind(Bindings.createObjectBinding(
                    {
                        if (control.isTextOpaque) Color.WHITE
                        else Color.WHITE.opacity(control.colorOpacity)
                    }, control.textOpaqueProperty, control.colorOpacityProperty
                ))
                fontProperty().bind(Bindings.createObjectBinding(
                    {
                        Font.font(MonoFont, FontWeight.BOLD, (if (control.index < 10) 1.7 else 1.3) * control.radius)
                    }, control.indexProperty, control.radiusProperty
                ))
                layoutXProperty().bind(Bindings.createDoubleBinding(
                    {
                        pickerRadiusBinding.get() - boundsInLocal.width / 2
                    }, pickerRadiusBinding, control.indexProperty
                ))
                layoutYProperty().bind(Bindings.createDoubleBinding(
                    {
                        pickerRadiusBinding.get()
                    }, pickerRadiusBinding, control.indexProperty
                ))
            }

            // Update
            val updateListener = onChange<Any> {
                // Remove old
                clip.fillProperty().unbind()
                root.children.clear() // make circle & text have no parents
                // Create new
                clip = Shape.subtract(circle, text).apply {
                    fillProperty().bind(Bindings.createObjectBinding(
                        {
                            control.color.opacity(control.colorOpacity)
                        }, control.colorProperty, control.colorOpacityProperty
                    ))
                }
                root.children.setAll(text, clip)
            }
            control.indexProperty.addListener(updateListener)
            control.radiusProperty.addListener(updateListener)

            // Manually update the first time
            updateListener.changed(null, null, null)
        }

        override fun getSkinnable(): CLabel = control

        override fun getNode(): Node = root

        override fun dispose() {
            clip.fillProperty().unbind()
            root.children.remove(clip)

            text.textProperty().unbind()
            text.fillProperty().unbind()
            text.fontProperty().unbind()
            text.layoutXProperty().unbind()
            text.layoutYProperty().unbind()
            root.children.remove(text)

            circle.radiusProperty().unbind()
            circle.centerXProperty().unbind()
            circle.centerYProperty().unbind()

            root.prefWidthProperty().unbind()
            root.prefHeightProperty().unbind()
        }

    }

    // endregion

}

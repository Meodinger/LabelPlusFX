package ink.meodinger.lpfx.component

import ink.meodinger.lpfx.Config.MonoFont
import ink.meodinger.lpfx.util.color.opacity
import ink.meodinger.lpfx.util.property.*

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.geometry.VPos
import javafx.scene.layout.Region
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
) : Region() {

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

    init {
        val pickerRadiusBinding = Bindings.createDoubleBinding({ radius.coerceAtLeast(MIN_PICK_RADIUS) }, radiusProperty)

        prefWidthProperty().bind(pickerRadiusBinding * 2)
        prefHeightProperty().bind(pickerRadiusBinding * 2)

        val circle = Circle().apply {
            radiusProperty().bind(radiusProperty)
            centerXProperty().bind(pickerRadiusBinding)
            centerYProperty().bind(pickerRadiusBinding)
        }
        val text = Text().apply {
            textOrigin = VPos.CENTER

            textProperty().bind(indexProperty.asString())
            fillProperty().bind(Bindings.createObjectBinding(
                {
                    if (isTextOpaque) Color.WHITE else Color.WHITE.opacity(colorOpacity)
                }, textOpaqueProperty, colorOpacityProperty
            ))
            fontProperty().bind(Bindings.createObjectBinding(
                {
                    Font.font(MonoFont, FontWeight.BOLD, (if (index < 10) 1.7 else 1.3) * radius)
                }, indexProperty, radiusProperty
            ))
            layoutXProperty().bind(Bindings.createDoubleBinding(
                {
                    pickerRadiusBinding.get() - boundsInLocal.width / 2
                }, pickerRadiusBinding, indexProperty
            ))
            layoutYProperty().bind(Bindings.createDoubleBinding(
                {
                    pickerRadiusBinding.get()
                }, pickerRadiusBinding, indexProperty
            ))
        }

        // Update
        val updateListener = onChange<Any> {
            children.clear() // make circle & text have no parents
            children.setAll(text, Shape.subtract(circle, text).apply {
                fillProperty().bind(Bindings.createObjectBinding(
                    {
                        color.opacity(colorOpacity)
                    }, colorProperty, colorOpacityProperty
                ))
            })
        }
        indexProperty.addListener(updateListener)
        radiusProperty.addListener(updateListener)
    }

}

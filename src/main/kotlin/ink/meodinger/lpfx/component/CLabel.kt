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
        const val MIN_PICK_RADIUS = 16.0
    }

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(labelIndex)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int by indexProperty

    private val radiusProperty: DoubleProperty = SimpleDoubleProperty(labelRadius)
    fun radiusProperty(): DoubleProperty = radiusProperty
    var radius: Double by radiusProperty

    private val colorProperty: ObjectProperty<Color> = SimpleObjectProperty(labelColor)
    fun colorProperty(): ObjectProperty<Color> = colorProperty
    var color: Color by colorProperty

    private val colorOpacityProperty: DoubleProperty = SimpleDoubleProperty(1.0)
    fun colorOpacityProperty(): DoubleProperty = colorOpacityProperty
    var colorOpacity: Double by colorOpacityProperty

    private val textOpaqueProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun textOpaqueProperty(): BooleanProperty = textOpaqueProperty
    var isTextOpaque: Boolean by textOpaqueProperty

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
            children.setAll(Shape.subtract(circle, text).apply {
                fillProperty().bind(Bindings.createObjectBinding(
                    {
                        color.opacity(colorOpacity)
                    }, colorProperty, colorOpacityProperty
                ))
            }, text)
        }
        indexProperty.addListener(updateListener)
        radiusProperty.addListener(updateListener)
    }

}

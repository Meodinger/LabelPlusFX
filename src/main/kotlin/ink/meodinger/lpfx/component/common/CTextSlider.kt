package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.property.transform

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import kotlin.math.roundToInt


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * A Slider with percent label (in HBox)
 */
class CTextSlider : HBox() {

    private val slider = Slider()
    private val label = Label()

    private val initScaleProperty: DoubleProperty = SimpleDoubleProperty(0.0)
    fun initScaleProperty(): DoubleProperty = initScaleProperty
    var initScale: Double
        get() = initScaleProperty.get()
        set(value) {
            initScaleProperty.set(value.coerceAtLeast(minScale).coerceAtMost(maxScale))
        }

    private val minScaleProperty: DoubleProperty = slider.minProperty()
    fun minScaleProperty(): DoubleProperty = minScaleProperty
    var minScale: Double by minScaleProperty

    private val maxScaleProperty: DoubleProperty = slider.maxProperty()
    fun maxScaleProperty(): DoubleProperty = maxScaleProperty
    var maxScale: Double by maxScaleProperty

    private val scaleProperty: DoubleProperty = slider.valueProperty()
    fun scaleProperty(): DoubleProperty = scaleProperty
    var scale: Double by scaleProperty

    init {
        label.textProperty().bind(scaleProperty.transform { "${(it * 100).roundToInt()}%" })

        alignment = Pos.CENTER_LEFT
        children.addAll(slider, label)
    }
}

package info.meodinger.lpfx.component.common

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue

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
 * Location: info.meodinger.lpfx.component
 */

/**
 * A Slider with percent label (in HBox)
 */
class CTextSlider : HBox() {

    private val slider = Slider()
    private val label = Label()

    val initScaleProperty = SimpleDoubleProperty(0.0)
    val minScaleProperty: DoubleProperty = slider.minProperty()
    val maxScaleProperty: DoubleProperty = slider.maxProperty()
    val scaleProperty: DoubleProperty = slider.valueProperty()

    var initScale: Double
        get() = initScaleProperty.value
        set(value) {
            if (value >= 0) {
                initScaleProperty.value = value.coerceAtLeast(minScale).coerceAtMost(maxScale)
            } else {
                throw IllegalArgumentException(I18N["exception.illegal_argument.negative_scale"])
            }
        }
    var minScale: Double by minScaleProperty
    var maxScale: Double by maxScaleProperty
    var scale: Double by scaleProperty

    init {
        scaleProperty.addListener { _, _, newValue ->
            label.text = (newValue as Double * 100).roundToInt().toString() + "%"
        }

        alignment = Pos.CENTER
        children.addAll(slider, label)
    }
}
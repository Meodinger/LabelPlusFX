package info.meodinger.lpfx.component

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
                throw IllegalArgumentException("negative scale")
            }
        }
    var minScale: Double
        get() = minScaleProperty.value
        set(value) {
            minScaleProperty.value = value
        }
    var maxScale: Double
        get() = maxScaleProperty.value
        set(value) {
            maxScaleProperty.value = value
        }
    var scale: Double
        get() = scaleProperty.value
        set(value) {
            scaleProperty.value = value
        }

    init {
        scaleProperty.addListener { _, _, newValue ->
            label.text = (newValue as Double * 100).roundToInt().toString() + "%"
        }

        alignment = Pos.CENTER
        children.addAll(slider, label)
    }
}
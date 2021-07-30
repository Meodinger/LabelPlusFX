package info.meodinger.lpfx.component

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

    val minScaleProperty = slider.minProperty()
    val maxScaleProperty = slider.maxProperty()
    val scaleProperty = slider.valueProperty()

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
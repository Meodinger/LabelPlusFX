package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.timer.TimerTaskManager
import ink.meodinger.lpfx.util.property.*

import javafx.application.Platform
import javafx.beans.property.*
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import javafx.scene.text.Text


/**
 * Author: Meodinger
 * Date: 2021/11/21
 * Have fun with my code!
 */

class CRollerLabel(contentText: String = "") : Region() {

    companion object {
        private const val SHIFT_PERIOD_DEFAULT: Long = 400L
    }

    private val label = Label(contentText)
    private var displayText: String by label.textProperty()
    private var clipped = false

    private val tooltipProperty: ObjectProperty<Tooltip> = label.tooltipProperty()
    fun tooltipProperty(): ObjectProperty<Tooltip> = tooltipProperty
    var tooltip: Tooltip by tooltipProperty

    private val textFillProperty: ObjectProperty<Paint> = label.textFillProperty()
    fun textFillProperty(): ObjectProperty<Paint> = textFillProperty
    var textFill: Paint by textFillProperty

    private val textProperty: StringProperty = SimpleStringProperty(contentText)
    fun textProperty(): StringProperty = textProperty
    var text: String by textProperty

    private val shiftPeriodProperty: LongProperty = SimpleLongProperty(SHIFT_PERIOD_DEFAULT)
    fun shiftPeriodProperty(): LongProperty = shiftPeriodProperty
    var shiftPeriod: Long by shiftPeriodProperty

    private val rollerManager = TimerTaskManager(shiftPeriod, shiftPeriod) {
        Platform.runLater { displayText = roll(displayText) }
    }

    init {
        label.prefWidthProperty().bind(widthProperty())
        label.prefHeightProperty().bind(heightProperty())
        children.add(label)

        textProperty.addListener(onNew {
            clipped = Text(it).boundsInLocal.width >= prefWidth
            displayText = roll(" $text ")
            if (clipped) startRoll()
        })
        shiftPeriodProperty.addListener(onNew<Number, Long> {
            rollerManager.clear()
            rollerManager.delay = it
            rollerManager.period = it
            rollerManager.refresh()
            rollerManager.schedule()
        })

        rollerManager.schedule()
    }

    private fun roll(text: String): String {
        return if (!clipped) {
            stopRoll()
            text.trim()
        } else {
            text.substring(1, text.length) + text.substring(0, 1)
        }
    }

    fun startRoll() {
        if (rollerManager.running) return

        rollerManager.clear()
        rollerManager.refresh()
        rollerManager.schedule()
    }

    fun stopRoll() {
        rollerManager.clear()
    }

}

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

class CRollerLabel(contentText: String) : Region() {

    private var changed = false
    private var clipped = false
    private val label = Label(contentText)
    private var displayText: String by label.textProperty()

    private val shiftPeriodProperty: LongProperty = SimpleLongProperty(320)
    fun shiftPeriodProperty(): LongProperty = shiftPeriodProperty
    var shiftPeriod: Long by shiftPeriodProperty

    private val tooltipProperty: ObjectProperty<Tooltip> = label.tooltipProperty()
    fun tooltipProperty(): ObjectProperty<Tooltip> = tooltipProperty
    var tooltip: Tooltip by tooltipProperty

    private val textFillProperty: ObjectProperty<Paint> = label.textFillProperty()
    fun textFillProperty(): ObjectProperty<Paint> = textFillProperty
    var textFill: Paint by textFillProperty

    private val textProperty: StringProperty = SimpleStringProperty(contentText)
    fun textProperty(): StringProperty = textProperty
    var text: String by textProperty

    private val rollerManager = TimerTaskManager(shiftPeriod, shiftPeriod) {
        Platform.runLater {
            if (changed) {
                changed = false
                clipped = Text(text).boundsInLocal.width >= this@CRollerLabel.prefWidth

                displayText = roll(" $text ")
            } else {
                displayText = roll(displayText)
            }
        }
    }

    constructor(): this("")

    init {
        // label.layoutXProperty().bind(- this.widthProperty() / 2)
        // label.layoutYProperty().bind(- this.heightProperty() / 2)
        label.prefWidthProperty().bind(this.widthProperty())
        label.prefHeightProperty().bind(this.heightProperty())
        this.children.add(label)

        textProperty.addListener(onChange { changed = true })
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
        if (!clipped) return text
        if (text.length <= 1) return text
        return text.substring(1, text.length) + text.substring(0, 1)
    }

    fun startRoll() {
        rollerManager.clear()
        rollerManager.refresh()
        rollerManager.schedule()
    }

    fun stopRoll() {
        rollerManager.clear()
    }

}

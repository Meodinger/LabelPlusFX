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

/**
 * A Label that will be rolling if there is not enought space to layout all text
 */
class CRollerLabel(contentText: String = "") : Region() {

    companion object {
        private const val SHIFT_PERIOD_DEFAULT: Long = 400L
    }

    private val label = Label(contentText)

    private val textProperty: StringProperty = SimpleStringProperty(contentText)
    /**
     * The text the label should display
     */
    fun textProperty(): StringProperty = textProperty
    /**
     * @see textProperty
     */
    var text: String by textProperty

    private val shiftIntervalProperty: LongProperty = SimpleLongProperty(SHIFT_PERIOD_DEFAULT)
    /**
     * The interval between shifts
     */
    fun shiftIntervalProperty(): LongProperty = shiftIntervalProperty
    /**
     * @see shiftIntervalProperty
     */
    var shiftInterval: Long by shiftIntervalProperty

    private val tooltipProperty: ObjectProperty<Tooltip> = label.tooltipProperty()
    /**
     * An export to `Label::tooltipProperty()`
     * @see javafx.scene.control.Control.tooltipProperty
     */
    fun tooltipProperty(): ObjectProperty<Tooltip> = tooltipProperty
    /**
     * @see tooltipProperty
     */
    var tooltip: Tooltip by tooltipProperty

    private val textFillProperty: ObjectProperty<Paint> = label.textFillProperty()
    /**
     * An export to `Label::textFillProperty()`
     * @see javafx.scene.control.Labeled.textFill
     */
    fun textFillProperty(): ObjectProperty<Paint> = textFillProperty
    /**
     * @see textFillProperty
     */
    var textFill: Paint by textFillProperty

    private val rollerManager = TimerTaskManager(shiftInterval, shiftInterval) {
        Platform.runLater { displayText = roll(displayText) }
    }
    private var displayText: String by label.textProperty()
    private var clipped = false

    init {
        label.prefWidthProperty().bind(widthProperty())
        label.prefHeightProperty().bind(heightProperty())
        children.add(label)

        textProperty.addListener(onNew {
            clipped = Text(it).boundsInLocal.width >= prefWidth
            displayText = roll(" $text ")
            if (clipped) startRoll()
        })
        shiftIntervalProperty.addListener(onNew<Number, Long> {
            rollerManager.clear()
            rollerManager.delay = it
            rollerManager.period = it
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

    /**
     * Start the rolling
     */
    fun startRoll() {
        rollerManager.schedule()
    }

    /**
     * Stop the rolling
     */
    fun stopRoll() {
        rollerManager.clear()
    }

}

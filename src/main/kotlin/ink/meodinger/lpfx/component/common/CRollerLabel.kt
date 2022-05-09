package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.timer.TimerTaskManager
import ink.meodinger.lpfx.util.property.*

import javafx.application.Platform
import javafx.beans.property.*
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
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
class CRollerLabel(contentText: String = "") : Control() {

    companion object {
        private const val SHIFT_PERIOD_DEFAULT: Long = 400L
    }

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

    private val textFillProperty: ObjectProperty<Paint> = SimpleObjectProperty(Color.BLACK)
    /**
     * An export to `Label::textFillProperty()`
     * @see javafx.scene.control.Labeled.textFill
     */
    fun textFillProperty(): ObjectProperty<Paint> = textFillProperty
    /**
     * @see textFillProperty
     */
    var textFill: Paint by textFillProperty

    private var clipped = false
    private val rollerManager = TimerTaskManager(shiftInterval, shiftInterval) {
        Platform.runLater { displayText = roll(displayText) }
    }

    private val displayTextProperty: StringProperty = SimpleStringProperty(contentText)
    private var displayText: String by displayTextProperty

    init {
        textProperty.addListener(onNew {
            clipped = Text(it).boundsInLocal.width >= prefWidth
            displayText = roll(" $text ")
            if (clipped) startRoll()
        })
        shiftIntervalProperty.addListener(onNew<Number, Long> {
            rollerManager.clear()
            rollerManager.delay = it
            rollerManager.interval = it
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

    // region Skin

    /**
     * Create default Skin
     * @see javafx.scene.control.Control.createDefaultSkin
     */
    override fun createDefaultSkin(): Skin<CRollerLabel> = RollerLabelSkin(this)

    private class RollerLabelSkin(private val control: CRollerLabel) : Skin<CRollerLabel> {

        private val root = Pane()
        private val label = Label()

        init {
            root.apply root@{
                add(label) {
                    textProperty().bind(control.displayTextProperty)
                    textFillProperty().bind(control.textFillProperty)
                    prefWidthProperty().bind(this@root.widthProperty())
                    prefHeightProperty().bind(this@root.heightProperty())
                }
            }
        }

        override fun getSkinnable(): CRollerLabel = control

        override fun getNode(): Node = root

        override fun dispose() {
            label.apply {
                textProperty().unbind()
                textFillProperty().unbind()
                prefWidthProperty().unbind()
                prefHeightProperty().unbind()
                root.children.remove(this)
            }
        }

    }

    // endregion

}

package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.util.platform.TextFont
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.text.Font
import java.io.IOException


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * The preferences that user set while using
 */
object Preference : AbstractProperties() {

    const val WINDOW_SIZE        = "WindowSize"
    const val MAIN_DIVIDER       = "MainDivider"
    const val RIGHT_DIVIDER      = "RightDivider"
    const val TEXTAREA_FONT_SIZE = "TextAreaFontSize"
    const val LAST_UPDATE_NOTICE = "LastUpdateNotice"

    override val default = listOf(
        CProperty(WINDOW_SIZE, 900, 600),
        CProperty(MAIN_DIVIDER, 0.618),
        CProperty(RIGHT_DIVIDER, 0.618),
        CProperty(TEXTAREA_FONT_SIZE, 12),
        CProperty(LAST_UPDATE_NOTICE, 0)
    )

    private val windowSizeProperty: ListProperty<Double> = SimpleListProperty()
    fun windowSizeProperty(): ListProperty<Double> = windowSizeProperty
    var windowSize: ObservableList<Double> by windowSizeProperty

    private val mainDividerPositionProperty: DoubleProperty = SimpleDoubleProperty()
    fun mainDividerPositionProperty(): DoubleProperty = mainDividerPositionProperty
    var mainDividerPosition: Double by mainDividerPositionProperty

    private val rightDividerPositionProperty: DoubleProperty = SimpleDoubleProperty()
    fun rightDividerPositionProperty(): DoubleProperty = rightDividerPositionProperty
    var rightDividerPosition: Double by rightDividerPositionProperty

    private val textAreaFontProperty: ObjectProperty<Font> = SimpleObjectProperty()
    fun textAreaFontProperty(): ObjectProperty<Font> = textAreaFontProperty
    var textAreaFont: Font by textAreaFontProperty

    private val lastUpdateNoticeProperty: LongProperty = SimpleLongProperty()
    fun lastUpdateNoticeProperty(): LongProperty = lastUpdateNoticeProperty
    var lastUpdateNotice: Long by lastUpdateNoticeProperty

    init { useDefault() }

    @Throws(IOException::class)
    override fun load() {
        load(Options.preference, this)

        windowSize = FXCollections.observableList(this[WINDOW_SIZE]!!.asDoubleList())
        mainDividerPosition = this[MAIN_DIVIDER]!!.asDouble()
        rightDividerPosition = this[RIGHT_DIVIDER]!!.asDouble()
        textAreaFont = Font.font(TextFont, this[TEXTAREA_FONT_SIZE]!!.asDouble())
        lastUpdateNotice = this[LAST_UPDATE_NOTICE]!!.asLong()
    }

    @Throws(IOException::class)
    override fun save() {
        this[WINDOW_SIZE]!!.set(windowSize)
        this[MAIN_DIVIDER]!!.set(mainDividerPosition)
        this[RIGHT_DIVIDER]!!.set(rightDividerPosition)
        this[TEXTAREA_FONT_SIZE]!!.set(textAreaFont.size)
        this[LAST_UPDATE_NOTICE]!!.set(lastUpdateNotice)

        save(Options.preference, this)
    }

}

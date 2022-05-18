package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.string.replaceEOL

import com.fasterxml.jackson.annotation.*
import javafx.beans.binding.ObjectExpression
import javafx.beans.property.*
import javafx.scene.paint.Color


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx.type
 */

/**
 * A translation label
 */
@JsonIncludeProperties("index", "groupId", "x", "y", "text")
class TransLabel @JsonCreator constructor(
    @JsonProperty("index")   index:   Int    = 0,
    @JsonProperty("groupId") groupId: Int    = 0,
    @JsonProperty("x")       x:       Double = 0.0,
    @JsonProperty("y")       y:       Double = 0.0,
    @JsonProperty("text")    text:    String = ""
) {

    // TODO: Mark

    companion object {

        /**
         * Bind TransLabel's ColorProperty, This function should only be called in TransFile
         */
        @Deprecated(level = DeprecationLevel.WARNING, message = "Only in TransFile")
        fun installColor(transLabel: TransLabel, property: ObjectExpression<Color>) {
            transLabel.colorProperty.bind(property)
        }

        /**
         * Unbind TransLabel's ColorProperty, This function should only be called in TransFile
         */
        @Deprecated(level = DeprecationLevel.WARNING, message = "Only in TransFile")
        fun disposeColor(transLabel: TransLabel) {
            transLabel.colorProperty.unbind()
        }

    }

    // region Properties

    private val indexProperty: IntegerProperty = SimpleIntegerProperty(index)
    fun indexProperty(): IntegerProperty = indexProperty
    var index: Int
        get() = indexProperty.get()
        set(value) {
            if (value < 0)
                throw IllegalArgumentException(String.format(I18N["exception.trans_label.index_invalid.i"], value))
            indexProperty.set(value)
        }

    private val groupIdProperty: IntegerProperty = SimpleIntegerProperty(groupId)
    fun groupIdProperty(): IntegerProperty = groupIdProperty
    var groupId: Int
        get() = groupIdProperty.get()
        set(value) {
            if (value < 0)
                throw IllegalArgumentException(String.format(I18N["exception.trans_label.groupId_invalid.i"], value))
            groupIdProperty.set(value)
        }

    private val xProperty: DoubleProperty = SimpleDoubleProperty(x)
    fun xProperty(): DoubleProperty = xProperty
    var x: Double
        get() = xProperty.get()
        set(value) {
            if (value < 0 || value > 1)
                throw IllegalArgumentException(String.format(I18N["exception.trans_label.x_invalid.d"], value))
            xProperty.set(value)
        }

    private val yProperty: DoubleProperty = SimpleDoubleProperty(y)
    fun yProperty(): DoubleProperty = yProperty
    var y: Double
        get() = yProperty.get()
        set(value) {
            if (value < 0 || value > 1)
                throw IllegalArgumentException(String.format(I18N["exception.trans_label.y_invalid.d"], value))
            yProperty.set(value)
        }

    private val textProperty: StringProperty = SimpleStringProperty(text)
    fun textProperty(): StringProperty = textProperty
    var text: String by textProperty

    // endregion

    // region Additional

    private var colorProperty: ObjectProperty<Color> = SimpleObjectProperty()
    /**
     * This property will be bound with the color-property of group that groupId refers to.
     * The binding procedure should be done while TransFile initializing.
     */
    fun colorProperty(): ReadOnlyObjectProperty<Color> = colorProperty
    /**
     * @see colorProperty
     */
    val color: Color by colorProperty

    // endregion

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other == null) return false
        if (other !is TransLabel) return false
        if (other.hashCode() != hashCode()) return false

        if (index != other.index) return false
        if (groupId != other.groupId) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (text != other.text) return false
        return true
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString(): String = "TransLabel($index, $groupId, $x - $y, ${text.replaceEOL()})"

    // ----- Destruction----- //

    operator fun component1(): Int    = index
    operator fun component2(): Int    = groupId
    operator fun component3(): Double = x
    operator fun component4(): Double = y
    operator fun component5(): String = text

}


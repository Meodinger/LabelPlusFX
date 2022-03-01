package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import com.fasterxml.jackson.annotation.*
import javafx.beans.property.*


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

    // ----- Exception ----- //

    class TransLabelException(message: String) : RuntimeException(message) {
        companion object {
            fun indexInvalid(index: Int) =
                TransLabelException(String.format(I18N["exception.trans_label.index_invalid.i"], index))
            fun groupIdInvalid(groupId: Int) =
                TransLabelException(String.format(I18N["exception.trans_label.groupId_invalid.i"], groupId))
            fun xInvalid(x: Double) =
                TransLabelException(String.format(I18N["exception.trans_label.x_invalid.d"], x))
            fun yInvalid(y: Double) =
                TransLabelException(String.format(I18N["exception.trans_label.y_invalid.d"], y))
        }
    }

    // ----- Properties ----- //

    val indexProperty:   IntegerProperty = SimpleIntegerProperty()
    val groupIdProperty: IntegerProperty = SimpleIntegerProperty()
    val xProperty:       DoubleProperty  = SimpleDoubleProperty()
    val yProperty:       DoubleProperty  = SimpleDoubleProperty()
    val textProperty:    StringProperty  = SimpleStringProperty(text)

    var index: Int
        get() = indexProperty.get()
        set(value) {
            if (value < 0) throw TransLabelException.indexInvalid(value)
            indexProperty.set(value)
        }
    var groupId: Int
        get() = groupIdProperty.get()
        set(value) {
            if (value < 0) throw TransLabelException.groupIdInvalid(value)
            groupIdProperty.set(value)
        }
    var x: Double
        get() = xProperty.get()
        set(value) {
            if (value < 0 || value > 1) throw TransLabelException.xInvalid(value)
            xProperty.set(value)
        }
    var y: Double
        get() = yProperty.get()
        set(value) {
            if (value < 0 || value > 1) throw TransLabelException.yInvalid(value)
            yProperty.set(value)
        }
    var text: String by textProperty

    init {
        this.index = index
        this.groupId = groupId
        this.x = x
        this.y = y
    }

    // ----- Object ----- //

    fun clone(): TransLabel = TransLabel(index, groupId, x, y, text)

    override operator fun equals(other: Any?): Boolean {
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

    override fun toString(): String = "TransLabel($index, $groupId, $x - $y, ${text.replace("\n", "\\n")})"

    // ----- Destruction----- //

    operator fun component1(): Int    = index
    operator fun component2(): Int    = groupId
    operator fun component3(): Double = x
    operator fun component4(): Double = y
    operator fun component5(): String = text

}


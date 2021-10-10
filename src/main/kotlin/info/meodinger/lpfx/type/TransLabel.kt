package info.meodinger.lpfx.type

import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
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

    companion object {
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
    }

    val indexProperty = SimpleIntegerProperty()
    val groupIdProperty = SimpleIntegerProperty()
    val xProperty = SimpleDoubleProperty()
    val yProperty = SimpleDoubleProperty()
    val textProperty = SimpleStringProperty(text)

    var index: Int
        get() = indexProperty.value
        set(value) {
            if (value < 0) throw TransLabelException.indexInvalid(value)
            indexProperty.value = value
        }
    var groupId: Int
        get() = groupIdProperty.value
        set(value) {
            if (value < 0) throw TransLabelException.groupIdInvalid(value)
            groupIdProperty.value = value
        }
    var x: Double
        get() = xProperty.value
        set(value) {
            if (value < 0 || value > 1) throw TransLabelException.xInvalid(value)
            xProperty.value = value
        }
    var y: Double
        get() = yProperty.value
        set(value) {
            if (value < 0 || value > 1) throw TransLabelException.yInvalid(value)
            yProperty.value = value
        }
    var text: String by textProperty

    init {
        this.index = index
        this.groupId = groupId
        this.x = x
        this.y = y
    }

    // ----- Object ----- //

    fun clone(): TransLabel {
        return TransLabel(index, groupId, x, y, text)
    }

    override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TransLabel) return false
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

    override fun toString(): String = "TransLabel($index, $groupId, $x - $y, ${text.replace("\n", ",")})"

    // ----- Destruction----- //

    operator fun component1(): Int = index
    operator fun component2(): Int = groupId
    operator fun component3(): Double = x
    operator fun component4(): Double = y
    operator fun component5(): String = text
}


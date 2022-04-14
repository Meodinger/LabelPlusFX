package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.color.isColorHex

import com.fasterxml.jackson.annotation.*
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty


/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Have fun with my code!
 */

/**
 * A translation label group
 */
@JsonIncludeProperties("name", "color")
class TransGroup @JsonCreator constructor(
    @JsonProperty("name")  name:     String = "NewGroup@${ACC++}",
    @JsonProperty("color") colorHex: String = "66CCFF"
) {
    companion object {
        private var ACC = 0
    }

    // ----- Exception ----- //

    class TransGroupException(message: String) : RuntimeException(message) {
        companion object {
            fun nameInvalid(groupName: String) =
                TransGroupException(String.format(I18N["exception.trans_group.name_invalid.s"], groupName))
            fun colorInvalid(color: String) =
                TransGroupException(String.format(I18N["exception.trans_group.color_invalid.s"], color))
        }
    }

    // ----- Properties ----- //

    val nameProperty: StringProperty = SimpleStringProperty()
    val colorHexProperty: StringProperty = SimpleStringProperty()

    var name: String
        get() = nameProperty.get()
        set(value) {
            if (value.isEmpty()) throw TransGroupException.nameInvalid(value)
            for (c in value.toCharArray()) if (c == '|' || c.isWhitespace()) throw TransGroupException.nameInvalid(value)
            nameProperty.set(value)
        }
    var colorHex: String
        @JsonGetter("color") get() = colorHexProperty.get()
        @JsonSetter("color") set(value) {
            if (!value.isColorHex()) throw TransGroupException.colorInvalid(value)
            colorHexProperty.set(value)
        }

    init {
        this.name = name
        this.colorHex = colorHex
    }

    fun clone(): TransGroup = TransGroup(name, colorHex)

    override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TransGroup) return false
        if (other.hashCode() != hashCode()) return false
        if (name != other.name) return false
        if (colorHex != other.colorHex) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + colorHex.hashCode()
        return result
    }

    override fun toString(): String = "TransGroup(name=$name, color=$colorHex)"

    // ----- Destruction----- //

    operator fun component1(): String = name
    operator fun component2(): String = colorHex

}

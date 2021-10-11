package info.meodinger.lpfx.type

import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import com.fasterxml.jackson.annotation.JsonIncludeProperties
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonGetter
import javafx.beans.property.SimpleStringProperty


/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: info.meodinger.lpfx.type
 */

/**
 * A translation label group
 */
@JsonIncludeProperties("name", "color")
class TransGroup @JsonCreator constructor(
    @JsonProperty("name")  name:     String = "NewGroup@${index++}",
    @JsonProperty("color") colorHex: String = "66CCFF"
) {
    companion object {
        private var index = 0
    }

    // ----- Exception ----- //

    class TransGroupException(message: String) : RuntimeException(message) {
        companion object {
            fun nameInvalid(groupName: String) =
                TransGroupException(String.format(I18N["exception.trans_group.name_invalid.format.s"], groupName))
            fun colorInvalid(color: String) =
                TransGroupException(String.format(I18N["exception.trans_group.color_invalid.format.s"], color))
        }
    }

    // ----- Properties ----- //

    val nameProperty = SimpleStringProperty()
    val colorHexProperty = SimpleStringProperty()

    var name: String
        get() = nameProperty.value
        set(value) {
            if (value.isEmpty()) throw TransGroupException.nameInvalid(value)
            for (c in value.toCharArray()) if (c == '|' || c.isWhitespace()) throw TransGroupException.nameInvalid(value)
            nameProperty.value = value
        }
    var colorHex: String
        @JsonGetter("color")
        get() = colorHexProperty.value
        //@JsonSetter("color")
        set(value) {
            if (!isColorHex(value)) throw TransGroupException.colorInvalid(value)
            colorHexProperty.value = value
        }

    init {
        this.name = name
        this.colorHex = colorHex
    }

    fun clone(): TransGroup {
        return TransGroup(name, colorHex)
    }

    override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TransGroup) return false
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

}
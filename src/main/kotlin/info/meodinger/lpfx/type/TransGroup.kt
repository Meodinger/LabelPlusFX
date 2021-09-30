package info.meodinger.lpfx.type

import info.meodinger.lpfx.util.color.isColorHex
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import com.fasterxml.jackson.annotation.JsonIncludeProperties
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
class TransGroup(
    name: String = "NewGroup@${index++}",
    color: String = "66CCFF"
) {
    companion object {
        private var index = 0

        class TransGroupException(message: String) : RuntimeException(message) {
            companion object {
                fun nameInvalid(groupName: String) =
                    TransGroupException(String.format(I18N["exception.trans_group.name_invalid.format.s"], groupName))
                fun colorInvalid(color: String) =
                    TransGroupException(String.format(I18N["exception.trans_group.color_invalid.format.s"], color))
            }
        }
    }

    val nameProperty = SimpleStringProperty()
    val colorProperty = SimpleStringProperty()

    var name: String
        get() = nameProperty.value
        set(value) {
            if (value.isEmpty()) throw TransGroupException.nameInvalid(value)
            for (c in value.toCharArray()) if (c == '|' || c.isWhitespace()) throw TransGroupException.nameInvalid(value)
            nameProperty.value = value
        }
    var color: String
        get() = colorProperty.value
        set(value) {
            if (!isColorHex(value)) throw TransGroupException.colorInvalid(value)
            colorProperty.value = value
        }

    init {
        this.name = name
        this.color = color
    }

    override fun toString(): String = "TransGroup(name=$name, color=$color)"

    override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TransGroup) return false
        if (name != other.name) return false
        if (color != other.color) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }
}
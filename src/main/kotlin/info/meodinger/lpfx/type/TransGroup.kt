package info.meodinger.lpfx.type

import com.fasterxml.jackson.annotation.JsonIgnore

import javafx.beans.property.SimpleStringProperty

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: info.meodinger.lpfx.type
 */
class TransGroup(
    name: String = "NewGroup@${index++}",
    color: String = "66CCFF"
) {
    companion object {
        private var index = 0
    }

    @JsonIgnore
    val nameProperty = SimpleStringProperty(name)
    @JsonIgnore
    val colorProperty = SimpleStringProperty(color)

    var name: String
        get() = nameProperty.value
        set(value) {
            nameProperty.value = value
        }
    var color: String
        get() = colorProperty.value
        set(value) {
            colorProperty.value = value
        }

    override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TransGroup) return false
        if (name != other.name) return false
        if (color != other.color) return false
        return true
    }

    override fun hashCode(): Int {
        var result = nameProperty.hashCode()
        result = 31 * result + colorProperty.hashCode()
        return result
    }
}
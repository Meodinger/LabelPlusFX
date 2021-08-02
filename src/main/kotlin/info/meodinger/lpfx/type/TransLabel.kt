package info.meodinger.lpfx.type

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
 */
// 'xProperty' not work, export field as 'xproperty', so like this
@JsonIgnoreProperties("indexProperty", "xproperty", "yproperty", "groupIdProperty", "textProperty")
class TransLabel(
    index: Int = 0,
    x: Double = 0.0,
    y: Double = 0.0,
    groupId: Int = 0,
    text: String = ""
) {
    val indexProperty = SimpleIntegerProperty(index)
    val xProperty = SimpleDoubleProperty(x)
    val yProperty = SimpleDoubleProperty(y)
    val groupIdProperty = SimpleIntegerProperty(groupId)
    val textProperty = SimpleStringProperty(text)

    var index: Int
        get() = indexProperty.value
        set(value) {
            indexProperty.value = value
        }
    var x: Double
        get() = xProperty.value
        set(value) {
            xProperty.value = value
        }
    var y: Double
        get() = yProperty.value
        set(value) {
            yProperty.value = value
        }
    var groupId: Int
        get() = groupIdProperty.value
        set(value) {
            groupIdProperty.value = value
        }
    var text: String
        get() = textProperty.value
        set(value) {
            textProperty.value = value
        }

    override operator fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is TransLabel) return false
        if (index != other.index) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (groupId != other.groupId) return false
        if (text != other.text) return false
        return true
    }

    override fun hashCode(): Int {
        var result = indexProperty.hashCode()
        result = 31 * result + xProperty.hashCode()
        result = 31 * result + yProperty.hashCode()
        result = 31 * result + groupIdProperty.hashCode()
        result = 31 * result + textProperty.hashCode()
        return result
    }
}


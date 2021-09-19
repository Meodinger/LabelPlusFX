package info.meodinger.lpfx.type

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.getValue
import tornadofx.setValue


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.type
 */

/**
 * A translation label
 */
// 'xProperty' not work because jackson exports field as 'xproperty'
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

    var index: Int by indexProperty
    var x: Double by xProperty
    var y: Double by yProperty
    var groupId: Int by groupIdProperty
    var text: String by textProperty

    override fun toString(): String = "TransLabel($index, $x - $y, $groupId, ${text.replace("\n", ",")})"

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


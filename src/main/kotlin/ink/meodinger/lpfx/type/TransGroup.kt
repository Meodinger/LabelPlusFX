package ink.meodinger.lpfx.type

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.color.isColorHex
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.readonly
import ink.meodinger.lpfx.util.property.transform

import com.fasterxml.jackson.annotation.*
import javafx.beans.property.*
import javafx.scene.paint.Color


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

    // region Properties

    private val nameProperty: StringProperty = SimpleStringProperty(name)
    fun nameProperty(): StringProperty = nameProperty
    var name: String
        get() = nameProperty.get()
        set(value) {
            if (value.isEmpty() || value.any { it == '|' || it.isWhitespace() })
                throw IllegalArgumentException(String.format(I18N["exception.trans_group.name_invalid.s"], value))
            nameProperty.set(value)
        }

    private val colorHexProperty: StringProperty = SimpleStringProperty(colorHex)
    fun colorHexProperty(): StringProperty = colorHexProperty
    var colorHex: String
        @JsonGetter("color") get() = colorHexProperty.get()
        @JsonSetter("color") set(value) {
            if (!value.isColorHex())
                throw IllegalArgumentException(String.format(I18N["exception.trans_group.color_invalid.s"], value))
            colorHexProperty.set(value)
        }

    // endregion

    // region Additional

    private val colorProperty: ReadOnlyObjectProperty<Color> = colorHexProperty.transform(Color::web).readonly()
    /**
     * This property is transformed from [colorHexProperty] by `Color::web`
     */
    fun colorProperty(): ReadOnlyObjectProperty<Color> = colorProperty
    /**
     * @see colorProperty
     */
    val color: Color by colorProperty

    // endregion

    override fun toString(): String = "TransGroup(name=$name, color=$colorHex)"

    // region Destruction

    operator fun component1(): String = name
    operator fun component2(): String = colorHex

    // endregion

}

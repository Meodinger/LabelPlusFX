package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane


/**
 * Author: Meodinger
 * Date: 2021/9/26
 * Have fun with my code!
 */

/**
 * A Label when double-clicked will become a TextField
 */
class CInputLabel : Pane() {

    companion object {
        private const val DEFAULT_WIDTH: Double = 48.0
        private const val DEFAULT_HEIGHT: Double = 24.0
    }

    private val label = Label()
    private val field = TextField()

    private val textFormatterProperty: ObjectProperty<TextFormatter<String>> = SimpleObjectProperty()
    fun textFormatterProperty(): ObjectProperty<TextFormatter<String>> = textFormatterProperty
    var textFormatter: TextFormatter<String> by textFormatterProperty

    private val editingProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun editingProperty(): BooleanProperty = editingProperty
    var isEditing: Boolean by editingProperty

    private val labelTextProperty: StringProperty = label.textProperty()
    fun labelTextProperty(): StringProperty = labelTextProperty
    var labelText: String by labelTextProperty

    private val fieldTextProperty: StringProperty = field.textProperty()
    fun fieldTextProperty(): StringProperty = fieldTextProperty
    var fieldText: String by fieldTextProperty

    var text: String
        get() {
            return if (isEditing) fieldText else labelText
        }
        set(value) {
            if (isEditing) fieldText = value else labelText = value
        }

    private val onChangeToFieldProperty: ObjectProperty<CInputLabel.() -> Unit> = SimpleObjectProperty {
        if (!fieldTextProperty.isBound) fieldText = labelText
    }
    fun onChangeToFieldProperty(): ObjectProperty<CInputLabel.() -> Unit> = onChangeToFieldProperty
    val onChangeToField: CInputLabel.() -> Unit by onChangeToFieldProperty

    /**
     * This function will be called when Label is double-clicked.
     * FieldText will be set to the return value of this function.
     * Then Label will hide and TextField will show.
     */
    fun setOnChangeToField(callback: CInputLabel.() -> Unit) = onChangeToFieldProperty.set(callback)

    private val onChangeToLabelProperty: ObjectProperty<CInputLabel.() -> Unit> = SimpleObjectProperty {
        if (!labelTextProperty.isBound) labelText = fieldText
    }
    fun onChangeToLabelProperty(): ObjectProperty<CInputLabel.() -> Unit> = onChangeToLabelProperty
    val onChangeToLabel: CInputLabel.() -> Unit by onChangeToLabelProperty

    /**
     * This function will be called when enter-key fired on TextField.
     * LabelText will be set to the return value of this function.
     * Then TextField will hide and Label will show.
     */
    fun setOnChangeToLabel(callback: CInputLabel.() -> Unit) = onChangeToLabelProperty.set(callback)

    init {
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)

        label.prefWidthProperty().bind(prefWidthProperty())
        label.prefHeightProperty().bind(prefHeightProperty())
        field.prefWidthProperty().bind(prefWidthProperty())
        field.prefHeightProperty().bind(prefHeightProperty())
        field.textFormatterProperty().bind(textFormatterProperty)

        editingProperty.addListener(onNew {
            children.clear()
            children.add(if (it) field else label)
        })

        label.setOnMouseClicked {
            if (it.button != MouseButton.PRIMARY) return@setOnMouseClicked
            if (!it.isDoubleClick) return@setOnMouseClicked

            onChangeToField(this)
            isEditing = true
        }
        field.setOnAction {
            onChangeToLabel(this)
            isEditing = false
        }

        children.add(label)
    }
}

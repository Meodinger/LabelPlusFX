package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.util.Callback


/**
 * Author: Meodinger
 * Date: 2021/9/26
 * Location: ink.meodinger.lpfx.component
 */

/**
 * A Label when double-clicked will become a TextField
 */
class CInputLabel : Pane() {

    companion object {
        const val DEFAULT_WIDTH: Double = 48.0
        const val DEFAULT_HEIGHT: Double = 24.0
    }

    private val label = Label()
    private val field = TextField()

    val textFormatterProperty: ObjectProperty<TextFormatter<String>> = SimpleObjectProperty()
    var textFormatter: TextFormatter<String> by textFormatterProperty

    val isEditingProperty: BooleanProperty = SimpleBooleanProperty(false)
    var isEditing: Boolean by isEditingProperty

    fun labelTextProperty() = label.textProperty()
    fun fieldTextProperty() = field.textProperty()
    var labelText: String by label.textProperty()
    var fieldText: String by field.textProperty()
    var text: String
        get() {
            return if (isEditing) fieldText else labelText
        }
        set(value) {
            if (isEditing) fieldText = value else labelText = value
        }

    val onChangeStartProperty: ObjectProperty<Callback<String, Unit>> = SimpleObjectProperty(Callback {})
    val onChangeStart: Callback<String, Unit> by onChangeStartProperty
    fun setOnChangeStart(callback: Callback<String, Unit>) {
        onChangeStartProperty.value = callback
    }

    val onChangeFinishProperty: ObjectProperty<Callback<String, Unit>> = SimpleObjectProperty(Callback {})
    val onChangeFinish: Callback<String, Unit> by onChangeFinishProperty
    fun setOnChangeFinish(callback: Callback<String, Unit>) {
        onChangeFinishProperty.value = callback
    }

    init {
        this.setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)

        label.prefWidthProperty().bind(this.prefWidthProperty())
        label.prefHeightProperty().bind(this.prefHeightProperty())
        field.prefWidthProperty().bind(this.prefWidthProperty())
        field.prefHeightProperty().bind(this.prefHeightProperty())
        field.textFormatterProperty().bind(textFormatterProperty)

        isEditingProperty.addListener { _, _, newValue ->
            this.children.clear()
            this.children.add(if (newValue) field else label)
        }

        label.setOnMouseClicked {
            if (it.button != MouseButton.PRIMARY) return@setOnMouseClicked
            if (it.clickCount < 2) return@setOnMouseClicked

            fieldText = labelText

            isEditing = true

            onChangeStart.call(labelText)
        }
        field.setOnAction {
            labelText = fieldText

            isEditing = false

            onChangeFinish.call(fieldText)
        }

        this.children.add(label)
    }
}
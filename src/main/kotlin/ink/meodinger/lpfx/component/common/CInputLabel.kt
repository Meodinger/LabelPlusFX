package ink.meodinger.lpfx.component.common

import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import java.util.function.Consumer


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
        const val DEFAULT_WIDTH: Double = 48.0
        const val DEFAULT_HEIGHT: Double = 24.0
    }

    private val label = Label()
    private val field = TextField()

    private val textFormatterProperty: ObjectProperty<TextFormatter<String>> = SimpleObjectProperty()
    fun textFormatterProperty(): ObjectProperty<TextFormatter<String>> = textFormatterProperty
    var textFormatter: TextFormatter<String> by textFormatterProperty

    private val isEditingProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun editingProperty(): BooleanProperty = isEditingProperty
    var isEditing: Boolean by isEditingProperty

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

    private val onChangeStartProperty: ObjectProperty<Consumer<String>> = SimpleObjectProperty(Consumer {})
    fun onChangeStartProperty(): ObjectProperty<Consumer<String>> = onChangeStartProperty
    val onChangeStart: Consumer<String> by onChangeStartProperty
    fun setOnChangeStart(callback: Consumer<String>) {
        onChangeStartProperty.value = callback
    }

    private val onChangeFinishProperty: ObjectProperty<Consumer<String>> = SimpleObjectProperty(Consumer {})
    fun onChangeFinishProperty(): ObjectProperty<Consumer<String>> = onChangeFinishProperty
    val onChangeFinish: Consumer<String> by onChangeFinishProperty
    fun setOnChangeFinish(callback: Consumer<String>) {
        onChangeFinishProperty.value = callback
    }

    init {
        this.setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT)

        label.prefWidthProperty().bind(this.prefWidthProperty())
        label.prefHeightProperty().bind(this.prefHeightProperty())
        field.prefWidthProperty().bind(this.prefWidthProperty())
        field.prefHeightProperty().bind(this.prefHeightProperty())
        field.textFormatterProperty().bind(this.textFormatterProperty)

        this.isEditingProperty.addListener { _, _, newValue ->
            this.children.clear()
            this.children.add(if (newValue) field else label)
        }

        label.setOnMouseClicked {
            if (it.button != MouseButton.PRIMARY) return@setOnMouseClicked
            if (it.clickCount < 2) return@setOnMouseClicked

            fieldText = labelText

            isEditing = true

            onChangeStart.accept(labelText)
        }
        field.setOnAction {
            labelText = fieldText

            isEditing = false

            onChangeFinish.accept(fieldText)
        }

        this.children.add(label)
    }
}
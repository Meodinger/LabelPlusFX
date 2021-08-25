package info.meodinger.lpfx.component

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import javafx.scene.text.TextAlignment

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */
class CComboBox<T> : HBox() {

    private val comboBox = ComboBox<T>()
    private val back = Button("<")
    private val next = Button(">")
    private var index = 0
    private var size = 0

    val valueProperty: ObjectProperty<T> = comboBox.valueProperty()
    val isWrappedProperty = SimpleBooleanProperty(false)

    val value: T
        get() = valueProperty.value
    var isWrapped: Boolean
        get() = isWrappedProperty.value
        set(value) {
            isWrappedProperty.value = value
        }

    init {
        comboBox.valueProperty().addListener { _, _, newValue -> index = comboBox.items.indexOf(newValue) }
        back.setOnMouseClicked { back() }
        next.setOnMouseClicked { next() }

        comboBox.prefWidth = 150.0
        back.textAlignment = TextAlignment.CENTER
        next.textAlignment = TextAlignment.CENTER
        children.addAll(comboBox, back, next)
    }

    fun reset() {
        comboBox.items.clear()
        comboBox.value = null
        index = 0
        size = 0
    }

    fun setList(list: List<T>) {
        if (list.isEmpty()) return

        reset()
        size = list.size
        comboBox.items.addAll(list)
        comboBox.value = comboBox.items[0]
    }

    fun back() {
        if (isWrapped) {
            if (index <= 0) index += size
        }
        if (index > 0) {
            comboBox.value = comboBox.items[--index]
        }
    }

    fun next() {
        if (isWrapped) {
            if (index >= size - 1) index -= size
        }
        if (index < size - 1) {
            comboBox.value = comboBox.items[++index]
        }
    }

    fun moveTo(index: Int) {
        if (index in 0 until size) {
            comboBox.value = comboBox.items[index]
        }
    }

    fun moveTo(item: T) {
        if (comboBox.items.contains(item)) comboBox.value = item
        else throw IllegalArgumentException("no such item")
    }

}
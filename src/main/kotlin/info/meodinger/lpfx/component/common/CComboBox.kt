package info.meodinger.lpfx.component.common

import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionModel
import javafx.scene.layout.HBox
import javafx.scene.text.TextAlignment


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */

/**
 * A ComboBox with back/next Button (in HBox)
 */
class CComboBox<T> : HBox() {

    private val comboBox = ComboBox<T>()
    private val back = Button("<")
    private val next = Button(">")

    val valueProperty: ObjectProperty<T> = comboBox.valueProperty()
    val value: T by valueProperty

    val indexProperty: ReadOnlyIntegerProperty = comboBox.selectionModel.selectedIndexProperty()
    val index: Int by indexProperty

    val isWrappedProperty = SimpleBooleanProperty(false)
    var isWrapped: Boolean by isWrappedProperty

    val items: ObservableList<T> get() = comboBox.items
    val selectionModel: SelectionModel<T> get() = comboBox.selectionModel

    init {
        back.setOnMouseClicked { back() }
        next.setOnMouseClicked { next() }

        comboBox.prefWidth = 150.0
        back.textAlignment = TextAlignment.CENTER
        next.textAlignment = TextAlignment.CENTER

        children.addAll(comboBox, back, next)
    }

    fun reset() {
        comboBox.items.clear()
        comboBox.selectionModel.clearSelection()
    }

    fun setList(list: List<T>) {
        reset()
        comboBox.items.addAll(list)

        if (list.isNotEmpty()) comboBox.selectionModel.select(0)
    }

    fun back() {
        val size = comboBox.items.size
        var newIndex = index - 1

        if (isWrapped) {
            if (newIndex < 0) newIndex += size
        }
        if (newIndex >= 0) {
            comboBox.value = comboBox.items[newIndex]
        }
    }

    fun next() {
        val size = comboBox.items.size
        var newIndex = index + 1

        if (isWrapped) {
            if (newIndex > size - 1) newIndex -= size
        }
        if (newIndex <= size - 1) {
            comboBox.value = comboBox.items[newIndex]
        }
    }

    fun moveTo(index: Int) {
        if (index in 0 until comboBox.items.size) comboBox.selectionModel.select(index)
        else throw IllegalArgumentException("index $index invalid")
    }

    fun moveTo(item: T) {
        if (comboBox.items.contains(item)) comboBox.selectionModel.select(item)
        else throw IllegalArgumentException("no item $item")
    }

}
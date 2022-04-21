package ink.meodinger.lpfx.util.property

import ink.meodinger.lpfx.type.TransFile
import javafx.beans.property.*
import javafx.collections.*
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Author: Meodinger
 * Date: 2022/4/20
 * Have fun with my code!
 */


class ExtensionTest {

    @Test
    fun observableKeysTest() {
        val mapProperty = SimpleMapProperty(FXCollections.observableMap(mutableMapOf(1 to 1)))
        val observableKeys = mapProperty.observableKeySet()
        var changed = false

        // Make sure the change invoke the listener
        observableKeys.addListener(SetChangeListener { changed = true })

        assertEquals(setOf(1), observableKeys)
        mapProperty[2] = 2
        assertEquals(setOf(1, 2), observableKeys)

        assert(changed)
    }

    @Test
    fun observableListSortedTest() {
        val listProperty = SimpleListProperty(FXCollections.observableList(mutableListOf(1)))
        val observableListSorted = listProperty.get().observableSorted { it }
        var changed = false

        // Make sure the change invoke the listener
        observableListSorted.addListener(ListChangeListener { changed = true })

        assertEquals(listOf(1), observableListSorted)
        listProperty.add(2)
        assertEquals(listOf(1, 2), observableListSorted)

        assert(changed)
    }
    @Test
    fun observableSetSortedTest() {
        val setProperty = SimpleSetProperty(FXCollections.observableSet(mutableSetOf(1)))
        val observableSetSorted = setProperty.get().observableSorted { it.sorted() }
        var changed = false

        // Make sure the change invoke the listener
        observableSetSorted.addListener(ListChangeListener { changed = true })

        assertEquals(listOf(1), observableSetSorted)
        setProperty.add(2)
        assertEquals(listOf(1, 2), observableSetSorted)

        assert(changed)
    }

    @Test
    fun crossTest() {
        val file = TransFile()

        val map = file.transMapObservable
        val keys = map.observableKeySet()
        val sorted = keys.observableSorted(Set<String>::sorted)
        var changed = false

        // Make sure the change invoke the listener
        sorted.addListener(ListChangeListener { changed = true })

        assertEquals(emptyList<String>(), sorted)
        file.transMapObservable["1"] = FXCollections.emptyObservableList()
        assertEquals(listOf("1"), sorted)
        file.transMapObservable.remove("1")
        assertEquals(emptyList<String>(), sorted)

        assert(changed)
    }

}

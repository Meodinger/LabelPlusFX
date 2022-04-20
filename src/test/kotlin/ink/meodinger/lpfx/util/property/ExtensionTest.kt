package ink.meodinger.lpfx.util.property

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleMapProperty
import javafx.beans.property.SimpleSetProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.SetChangeListener
import org.junit.Test

/**
 * Author: Meodinger
 * Date: 2022/4/20
 * Have fun with my code!
 */


class ExtensionTest {

    @Test
    fun observableKeysTest() {
        val mapProperty = SimpleMapProperty(FXCollections.observableMap(mutableMapOf(1 to -1)))
        val observableKeys = mapProperty.observableKeys()
        var changed = false

        observableKeys.addListener(SetChangeListener { changed = true })

        assert(observableKeys.contains(1))
        mapProperty[2] = -2
        assert(observableKeys.contains(2))

        assert(changed)
    }

    @Test
    fun observableListSortedTest() {
        val listProperty = SimpleListProperty(FXCollections.observableList(mutableListOf(0)))
        val observableListSorted = listProperty.get().observableSorted { it }
        var changed = false

        observableListSorted.addListener(ListChangeListener { changed = true })

        assert(observableListSorted.size == 1)
        assert(observableListSorted[0] == 0)
        listProperty.add(1)
        assert(observableListSorted.size == 2)
        assert(observableListSorted[1] == 1)

        assert(changed)
    }
    @Test
    fun observableSetSortedTest() {
        val setProperty = SimpleSetProperty(FXCollections.observableSet(mutableSetOf(0)))
        val observableSetSorted = setProperty.get().observableSorted { it.sorted() }
        var changed = false

        observableSetSorted.addListener(ListChangeListener { changed = true })

        assert(observableSetSorted.size == 1)
        assert(observableSetSorted[0] == 0)
        setProperty.add(1)
        assert(observableSetSorted.size == 2)
        assert(observableSetSorted[1] == 1)

        assert(changed)
    }

    @Test
    fun crossTest() {
        val mapProperty = SimpleMapProperty(FXCollections.observableMap(mutableMapOf(0 to 0)))
        val observableKeys = mapProperty.observableKeys()
        val observableSetSorted = observableKeys.observableSorted { it.sorted() }
        var changed = false

        observableSetSorted.addListener(ListChangeListener { changed = true })

        assert(observableSetSorted.size == 1)
        assert(observableSetSorted[0] == 0)
        mapProperty[1] = 1
        assert(observableSetSorted.size == 2)
        assert(observableSetSorted[1] == 1)

        assert(changed)
    }

}

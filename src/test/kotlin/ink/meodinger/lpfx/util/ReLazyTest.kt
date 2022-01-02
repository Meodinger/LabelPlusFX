package ink.meodinger.lpfx.util

import org.junit.Assert.assertEquals

import org.junit.Test

/**
 * Author: Meodinger
 * Date: 2022/1/2
 * Location: ink.meodinger.lpfx.util
 */
class ReLazyTest {

    private var _z: Int = 0
    private val z: Int get() = _z++

    @Test
    fun lazyTest() {
        _z = 0
        val a: Int by ReLazy { z }

        assertEquals(0, a)
        assertEquals(0, a)
        assertEquals(0, a)
    }

    @Test
    fun lazyReTest() {
        _z = 0
        val lazy = ReLazy { z }

        val a: Int by lazy
        assertEquals(0, a)
        assertEquals(0, a)

        lazy.refresh()
        assertEquals(1, a)
        assertEquals(1, a)
    }

}

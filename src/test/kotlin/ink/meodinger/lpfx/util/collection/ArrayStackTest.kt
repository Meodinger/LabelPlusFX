package ink.meodinger.lpfx.util.collection

import org.junit.Test

import org.junit.Assert.*

/**
 * Author: Meodinger
 * Date: 2022/3/11
 * Have fun with my code!
 */
class ArrayStackTest {

    @Test
    fun testIO() {
        val stack = ArrayStack<Int>()
        assertEquals(0, stack.size)
        stack.push(1)
        assertEquals(1, stack.size)
        stack.peek()
        assertEquals(1, stack.size)
        stack.pop()
        assertEquals(0, stack.size)
    }

    @Test
    fun empty() {
        val stack = ArrayStack<Int>()
        stack.push(1)
        stack.push(2)
        stack.push(3)
        assertEquals(false, stack.isEmpty())
        stack.empty()
        assertEquals(true, stack.isEmpty())
    }

    @Test
    fun isEmpty() {
        val stack = ArrayStack<Int>()
        assertEquals(true, stack.isEmpty())
        stack.push(1)
        assertEquals(false, stack.isEmpty())
        stack.pop()
        assertEquals(true, stack.isEmpty())
    }

}

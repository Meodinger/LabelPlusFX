package ink.meodinger.lpfx.util.collection


/**
 * Author: Meodinger
 * Date: 2022/3/11
 * Have fun with my code!
 */

/**
 * Alternative Stack
 */
interface Stack<E> {

    /**
     * Indicates the size of the stack
     */
    val size: Int

    /**
     * Push an element into the stack
     */
    fun push(element: E)

    /**
     * Pop the element at the top of the stack
     */
    fun pop(): E

    /**
     * Peek the element at the top of the stack
     */
    fun peek(): E

    /**
     * Empty the stack
     */
    fun empty()

    /**
     * Returns `true` if the stack is empty (contains no elements), `false` otherwise.
     */
    fun isEmpty(): Boolean

}

/**
 * Alias fot !isEmpty()
 */
fun <E> Stack<E>.isNotEmpty(): Boolean = !isEmpty()

/**
 * A Stack implemented with an array as internal storage.
 * Its read method `pop()` and `peek()` is faster than
 * `kotlin.collections.ArrayDeque`, but its write method
 * `push()` will be very slow if the stack is too large.
 */
class ArrayStack<E>(initialCapacity: Int = DEFAULT_CAPACITY) : Stack<E> {

    companion object {
        private const val DEFAULT_CAPACITY = 16

        /**
         * A soft maximum array length imposed by stack growth computations.
         * @see jdk.internal.util.ArraysSupport.SOFT_MAX_ARRAY_LENGTH
         */
        private const val SOFT_MAX_ARRAY_LENGTH = Int.MAX_VALUE - 8

    }

    private var _array: Array<Any?> = arrayOfNulls(initialCapacity)
    private var _pointer: Int = 0

    @Suppress("UNCHECKED_CAST")
    private fun elementData(index: Int): E = _array[index] as E

    private fun grow() {
        val increment = (_pointer shr 1).coerceAtLeast(1).coerceAtMost(SOFT_MAX_ARRAY_LENGTH - _pointer)
        val newSize = _pointer + increment
        if (newSize < 0) throw OutOfMemoryError("Required array length $_pointer + $increment is too large")

        _array = arrayOfNulls<Any>(newSize).also {
            System.arraycopy(_array, 0, it, 0, _pointer)
        }
    }

    override val size: Int get() = _pointer

    override fun push(element: E) {
        if (_pointer == _array.size) grow()
        _array[_pointer++] = element
    }

    override fun pop(): E {
        if (_pointer == 0) throw IllegalStateException("Stack has no elements")
        return elementData(--_pointer)
    }

    override fun peek(): E {
        if (_pointer == 0) throw IllegalStateException("Stack has no elements")
        return elementData(_pointer - 1)
    }

    override fun empty() {
        _pointer = 0
    }

    override fun isEmpty(): Boolean {
        return _pointer == 0
    }

}

package test.lpfx

import org.junit.Assert

/**
 * Author: Meodinger
 * Date: 2021/12/26
 * Location: test.lpfx
 */


/**
 * Assert an Exception will be thrown during callable running
 */
fun <T : Throwable> assertThrow(clazz: Class<T>, callable: () -> Unit) {
    try {
        callable()
    } catch (e: Throwable) {
        Assert.assertTrue(e::class.qualifiedName, clazz.isInstance(e))
    }
}

/**
 * Assert no Exceptions will be thrown during callable running
 */
fun assertNotThrow(callable: () -> Unit) {
    try {
        callable()
    } catch (e: Throwable) {
        Assert.assertTrue(e.message, false)
    }
}

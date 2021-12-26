package ink.meodinger.io

import ink.meodinger.lpfx.io.UpdateChecker.Version
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Author: Meodinger
 * Date: 2021/12/23
 * Have fun with my code!
 */

class VersionTest {

    private fun <T : Throwable> assertThrow(clazz: Class<T>, callable: () -> Unit) {
        try {
            callable()
        } catch (e: Throwable) {
            assertTrue(clazz.isInstance(e))
        }
    }

    private fun assertNotThrow(callable: () -> Unit) {
        try {
            callable()
        } catch (e: Throwable) {
            assertTrue(false)
        }
    }


    @Test
    fun testConstruct() {
        assertThrow(IllegalArgumentException::class.java) { Version(1, 1, 100) }
        assertThrow(IllegalArgumentException::class.java) { Version(1, 1, -1) }

        assertNotThrow { Version(1, 1, 1) }
        assertNotThrow { Version(1, 1, 0) }
        assertNotThrow { Version(1, 1, 99) }
    }

    @Test
    fun testOf() {
        val z0 = Version.of("v1.2.4")
        assertEquals(z0, Version.of("v1.2.4"))
        assertEquals(z0, Version.of("V1.2.4"))
        assertEquals(z0, Version.of("1.2.4"))

        assertEquals(Version.V0, Version.of("z1.2.4"))
        assertEquals(Version.V0, Version.of("a-1.2.4"))

        assertThrow(IllegalArgumentException::class.java) { Version.of("v1.1.100") }
        assertThrow(IllegalArgumentException::class.java) { Version.of("100.1.1") }
    }

    @Test
    fun testCompare() {
        assertTrue(Version(1, 2, 4) == Version(1, 2, 4))
        assertTrue(Version(1, 2, 4) > Version(0, 2, 4))
        assertTrue(Version(1, 2, 4) > Version(1, 1, 4))
        assertTrue(Version(1, 2, 4) > Version(1, 2, 3))
        assertTrue(Version(1, 2, 4) < Version(2, 2, 4))
        assertTrue(Version(1, 2, 4) < Version(1, 3, 4))
        assertTrue(Version(1, 2, 4) < Version(1, 2, 5))
    }

}

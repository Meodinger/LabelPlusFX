package ink.meodinger.io

import ink.meodinger.lpfx.util.Version
import test.lpfx.assertThrow
import test.lpfx.assertNotThrow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Author: Meodinger
 * Date: 2021/12/23
 * Have fun with my code!
 */

class VersionTest {

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
        assertEquals(Version.V0, Version.of("z1.2.4"))
        assertEquals(Version.V0, Version.of("v-1.2.4"))

        assertThrow(IllegalArgumentException::class.java) { Version.of("v1.1.100") }
        assertThrow(IllegalArgumentException::class.java) { Version.of("v1.100.1") }
        assertThrow(IllegalArgumentException::class.java) { Version.of("v100.1.1") }

        val z0 = Version(1, 4, 9)
        assertEquals(z0, Version.of("v1.4.9"))
        assertEquals(z0, Version.of("V1.4.9"))

        assertEquals(Version(11, 11, 1), Version.of("v11.11.1"))
        assertEquals(Version(11, 1, 11), Version.of("v11.1.11"))
        assertEquals(Version(1, 11, 11), Version.of("v1.11.11"))
    }

    @Test
    fun testCompare() {
        assertTrue(Version(1, 2, 3) == Version(1, 2, 3))
        assertTrue(Version(1, 2, 3) > Version(0, 2, 3))
        assertTrue(Version(1, 2, 3) > Version(1, 1, 3))
        assertTrue(Version(1, 2, 3) > Version(1, 2, 2))
        assertTrue(Version(1, 0, 0) > Version(0, 99, 99))
    }

}

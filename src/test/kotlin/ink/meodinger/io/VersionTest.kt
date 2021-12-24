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

    @Test
    fun testOf() {
        val z0 = Version.of("v1.2.4")
        assertEquals(z0, Version.of("v1.2.4"))
        assertEquals(z0, Version.of("V1.2.4"))
        assertEquals(z0, Version.of("1.2.4"))

        assertEquals(null, Version.of("z1.2.4"))
        assertEquals(null, Version.of("a-1.2.4"))
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

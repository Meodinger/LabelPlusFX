package ink.meodinger.lpfx.util.string

import org.junit.Test
import org.junit.Assert.*

/**
 * Author: Meodinger
 * Date: 2021/11/9
 * Have fun with my code!
 */

class CStringKtTest {

    private val names1 = listOf(
        "01.jpg", "02.jpg", "03.jpg", "04.jpg", "05.jpg",
        "06.jpg", "07.jpg", "08.jpg", "09.jpg", "10.jpg",
        "11.jpg", "21.jpg", "13.jpg"
    )
    private val expected1 = listOf(
        "01.jpg", "02.jpg", "03.jpg", "04.jpg", "05.jpg",
        "06.jpg", "07.jpg", "08.jpg", "09.jpg", "10.jpg",
        "11.jpg", "13.jpg", "21.jpg"
    )

    private val names2 = listOf(
        "image_1.jpg", "image_2.jpg", "image_3.jpg", "image_4.jpg", "image_5.jpg",
        "image_6.jpg", "image_7.jpg", "image_8.jpg", "image_9.jpg", "image_10.jpg",
        "image_11.jpg", "image_21.jpg", "image_13.jpg"
    )
    private val expected2 = listOf(
        "image_1.jpg", "image_2.jpg", "image_3.jpg", "image_4.jpg", "image_5.jpg",
        "image_6.jpg", "image_7.jpg", "image_8.jpg", "image_9.jpg", "image_10.jpg",
        "image_11.jpg", "image_13.jpg", "image_21.jpg",
    )

    @Test
    fun sortByDigitTest() {
        val sorted1 = sortByDigit(names1)
        for (i in sorted1.indices) assertEquals(expected1[i], sorted1[i])

        val sorted2 = sortByDigit(names2)
        for (i in sorted2.indices) assertEquals(expected2[i], sorted2[i])
    }

    @Test
    fun isMathNaturalTest()  {
        assertTrue("123".isMathematicalNatural())

        assertFalse("-123".isMathematicalNatural())
        assertFalse(".123".isMathematicalNatural())
        assertFalse("-.123".isMathematicalNatural())
        assertFalse(".-123".isMathematicalNatural())
        assertFalse("1.23".isMathematicalNatural())
        assertFalse("-1.23".isMathematicalNatural())

        assertFalse("-123.".isMathematicalNatural())
        assertFalse(".123.".isMathematicalNatural())
        assertFalse("-.123.".isMathematicalNatural())
        assertFalse(".-123.".isMathematicalNatural())
        assertFalse("1.23.".isMathematicalNatural())
        assertFalse("-1.23.".isMathematicalNatural())

        assertFalse("-123-".isMathematicalNatural())
        assertFalse(".123-".isMathematicalNatural())
        assertFalse("-.123-".isMathematicalNatural())
        assertFalse(".-123-".isMathematicalNatural())
        assertFalse("1.23-".isMathematicalNatural())
        assertFalse("-1.23-".isMathematicalNatural())
    }

    @Test
    fun isMathIntegerTest()  {
        assertTrue("123".isMathematicalInteger())

        assertTrue("-123".isMathematicalInteger())
        assertFalse(".123".isMathematicalInteger())
        assertFalse("-.123".isMathematicalInteger())
        assertFalse(".-123".isMathematicalInteger())
        assertFalse("1.23".isMathematicalInteger())
        assertFalse("-1.23".isMathematicalInteger())

        assertFalse("-123.".isMathematicalInteger())
        assertFalse(".123.".isMathematicalInteger())
        assertFalse("-.123.".isMathematicalInteger())
        assertFalse(".-123.".isMathematicalInteger())
        assertFalse("1.23.".isMathematicalInteger())
        assertFalse("-1.23.".isMathematicalInteger())

        assertFalse("-123-".isMathematicalInteger())
        assertFalse(".123-".isMathematicalInteger())
        assertFalse("-.123-".isMathematicalInteger())
        assertFalse(".-123-".isMathematicalInteger())
        assertFalse("1.23-".isMathematicalInteger())
        assertFalse("-1.23-".isMathematicalInteger())
    }

    @Test
    fun isMathDecimalTest()  {
        assertTrue("123".isMathematicalDecimal())

        assertTrue("-123".isMathematicalDecimal())
        assertTrue(".123".isMathematicalDecimal())
        assertTrue("-.123".isMathematicalDecimal())
        assertFalse(".-123".isMathematicalDecimal())
        assertTrue("1.23".isMathematicalDecimal())
        assertTrue("-1.23".isMathematicalDecimal())

        assertTrue("-123.".isMathematicalDecimal())
        assertFalse(".123.".isMathematicalDecimal())
        assertFalse("-.123.".isMathematicalDecimal())
        assertFalse(".-123.".isMathematicalDecimal())
        assertFalse("1.23.".isMathematicalDecimal())
        assertFalse("-1.23.".isMathematicalDecimal())

        assertFalse("-123-".isMathematicalDecimal())
        assertFalse(".123-".isMathematicalDecimal())
        assertFalse("-.123-".isMathematicalDecimal())
        assertFalse(".-123-".isMathematicalDecimal())
        assertFalse("1.23-".isMathematicalDecimal())
        assertFalse("-1.23-".isMathematicalDecimal())
    }

    @Test
    fun deleteTailTest() {
        // Empty tail
        assertEquals("1", StringBuilder("1").deleteTrailing("").toString())
        assertEquals("", StringBuilder("").deleteTrailing("").toString())

        // Empty this
        assertEquals("", StringBuilder("").deleteTrailing("1").toString())

        // Incorrect tail
        assertEquals("1", StringBuilder("1").deleteTrailing("0").toString())

        // run
        assertEquals("12", StringBuilder("123").deleteTrailing("3").toString())
        assertEquals("1", StringBuilder("123").deleteTrailing("23").toString())
        assertEquals("", StringBuilder("123").deleteTrailing("123").toString())
    }

    @Test
    fun fixedTest() {
        assertEquals("4.1234", 4.12344.fixed(4))
        assertEquals("0.1234", 0.12344.fixed(4))
        assertEquals("1.0000", 1.fixed(4))
    }

}

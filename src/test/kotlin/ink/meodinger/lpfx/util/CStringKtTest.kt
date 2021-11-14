package ink.meodinger.lpfx.util

import ink.meodinger.lpfx.util.string.*
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

    private val names3 = listOf(
        "5 (1).jpg", "5 (2).jpg", "5 (3).jpg", "5 (4).jpg", "5 (5).jpg",
        "5 (6).jpg", "5 (7).jpg", "5 (8).jpg", "5 (9).jpg", "5(10).jpg",
        "5(11).jpg", "5(21).jpg", "5(13).jpg"
    )
    private val expected3 = listOf(
        "5 (1).jpg", "5 (2).jpg", "5 (3).jpg", "5 (4).jpg", "5 (5).jpg",
        "5 (6).jpg", "5 (7).jpg", "5 (8).jpg", "5 (9).jpg", "5(10).jpg",
        "5(11).jpg", "5(13).jpg", "5(21).jpg"
    )

    @Test
    fun sortByDigit() {
        val sorted1 = sortByDigit(names1)
        for (i in sorted1.indices) assertEquals(expected1[i], sorted1[i])

        val sorted2 = sortByDigit(names2)
        for (i in sorted2.indices) assertEquals(expected2[i], sorted2[i])

        // val sorted3 = sortByDigit(names3)
        // for (i in sorted3.indices) assertEquals(expected3[i], sorted3[i])
    }
}
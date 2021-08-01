package type

import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: type
 */
fun fileTest() {
    val file = TransFile()
    file.version = intArrayOf(1, 0)
    file.comment = TransFile.DEFAULT_COMMENT
    file.groupList.add(TransGroup())
    file.transMap["0"] = listOf(TransLabel()).toMutableList()

    val another = file.clone()
    another.version = intArrayOf(1, 2)
    another.comment = TransFile.DEFAULT_COMMENT_LIST[1]
    another.groupList[0].name = "Edited"
    another.groupList.add(TransGroup())
    another.transMap["0"]!![0].text = "Edited"
    another.transMap["0"]!!.add(TransLabel(1, 0.0, 0.0, 0, ""))
    another.transMap["1"] = listOf(TransLabel()).toMutableList()
    println("""
        |----------
        |TransFile clone test
        |Version: ${!(file.version.contentEquals(another.version))}
        |Comment: ${file.comment != another.comment}
        |Groups: ${(file.groupList[0].name != another.groupList[0].name) && (file.groupList.size == 1)}
        |TransMap: ${(file.transMap["0"]!![0].text != another.transMap["0"]!![0].text) && (file.transMap["0"]!!.size == 1) && (file.transMap.keys.size == 1)}
        |----------
        """.trimIndent())
}
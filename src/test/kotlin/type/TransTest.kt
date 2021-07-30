package type

import info.meodinger.lpfx.type.TransFile
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.type.TransLabel

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: type
 */
fun labelTest() {
    println("Label init test: 0-0.1-0.2-0-2333")
    val label = TransLabel(0, 0.1, 0.2, 0, "2333")
    println(label)
    println("Label init test: nothing")
    println(TransLabel())
}

fun groupTest() {
    println("Group init test: g1-ffffff")
    val group = TransGroup("g1", "ffffff")
    println(group)
    println("Group init test: nothing")
    println(TransGroup())
    println(TransGroup())
}

fun fileTest() {
    println("File init test: with late init")
    val file = TransFile()
    file.version = intArrayOf(1, 0)
    file.comment = TransFile.DEFAULT_COMMENT
    file.groupList = listOf(TransGroup()).toMutableList()
    file.transMap = mapOf("0" to listOf(TransLabel()).toMutableList()).toMutableMap()
    println(file)
    println("File clone test")
    println(file)
    val another = file.clone()
    another.version = intArrayOf(1, 2)
    another.comment = TransFile.DEFAULT_COMMENT_LIST[1]
    another.groupList[0].name = "Edited"
    another.groupList.add(TransGroup())
    another.transMap["0"]!![0].text = "Edited"
    another.transMap["0"]!!.add(TransLabel(1, 0.0, 0.0, 0, ""))
    another.transMap["1"] = listOf(TransLabel()).toMutableList()
    println(another)
}
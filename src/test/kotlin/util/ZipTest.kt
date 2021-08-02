package util

import info.meodinger.lpfx.util.CZip
import info.meodinger.lpfx.util.resource.*
import java.io.ByteArrayInputStream

import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: util
 */
fun zipTest() {
    val zip = CZip("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\a.zip")
    var startTime = Date().time
    var endTime: Long

    print("Zip File: ")
    zip.zip(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\src\\main\\resources\\file\\script\\Meo_PS_Script"), "/file")
    endTime = Date().time
    println(endTime - startTime)

    startTime = endTime
    print("Zip InputStream: ")
    zip.zip(FileInputStream(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\src\\main\\resources\\file\\script\\Meo_PS_Script")), "/file_stream")
    endTime = Date().time
    println(endTime - startTime)

    startTime = endTime
    print("Zip ByteInputStream: ")
    zip.zip(ByteArrayInputStream(SCRIPT), "/byte_stream")
    endTime = Date().time
    println(endTime - startTime)

    startTime = endTime
    print("Zip ByteArray: ")
    zip.zip(SCRIPT, "/bytes")
    endTime = Date().time
    println(endTime - startTime)

    println("Zip end")
    zip.close()
}
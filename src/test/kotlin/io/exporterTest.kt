package io

import info.meodinger.lpfx.io.exportLP
import info.meodinger.lpfx.io.exportMeo

import java.io.File
import java.io.IOException

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: io
 */
fun exporterTest() {
    val transFile = commonTest()

    println("----- Exporter Test -----")

    print("LP: ")
    try {
        exportLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\export.txt"), transFile)
        println(true)
    } catch (e: IOException) {
        println(false)
        e.printStackTrace()
    }

    print("Meo: ")
    try {
        exportMeo(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\export.json"), transFile)
        println(true)
    } catch (e: IOException) {
        println(false)
        e.printStackTrace()
    }

}
package io

import info.meodinger.lpfx.io.exportLP
import info.meodinger.lpfx.io.exportMeo

import java.io.File

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: io
 */
fun exporterTest() {
    val transFile = commonTest()

    print("LP: ")
    println(exportLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\export.txt"), transFile))

    print("Meo: ")
    println(exportMeo(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\export.json"), transFile))
}
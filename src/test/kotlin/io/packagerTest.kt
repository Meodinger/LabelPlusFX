package io

import info.meodinger.lpfx.io.pack
import java.io.File
/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Location: io
 */
fun packTest() {
    print("Pack: ")
    println(pack(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\export.zip"), "D:\\WorkPlace\\Kotlin\\LabelPlusFX\\src\\test\\resources\\sample\\pics", commonTest()))
}
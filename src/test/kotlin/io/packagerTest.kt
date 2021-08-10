package io

import info.meodinger.lpfx.io.pack

import java.io.File
import java.io.IOException

/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Location: io
 */
fun packTest() {
    println("----- Packager Test -----")

    print("Pack: ")
    try {
        pack(
            File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\export.zip"),
            "D:\\WorkPlace\\Kotlin\\LabelPlusFX\\src\\test\\resources\\sample\\pics",
            commonTest()
        )
        println(true)
    } catch (e: IOException) {
        println(false)
        e.printStackTrace()
    }

}
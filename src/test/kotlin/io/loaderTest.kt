package io

import info.meodinger.lpfx.io.loadLP
import info.meodinger.lpfx.type.TransFile

import java.io.File

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: io
 */
fun loaderTest() {
    commonTest()
    comment_sticky_to_body_Test()
    empty_line_in_1_2_Test()
    empty_text_in_1_1_Test()
    text_sticky_to_next_1_1Test()
    too_many_groups_Test()
}

fun commonTest(): TransFile {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp.txt"))
    println("Done")
    return file
}

fun comment_sticky_to_body_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_comment_sticky_to_body.txt"))
    println("Done")
}

fun empty_line_in_1_2_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_empty_line_in_1-2.txt"))
    println("Done")
}

fun empty_text_in_1_1_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_empty_text_in_1-1.txt"))
    println("Done")
}

fun text_sticky_to_next_1_1Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_text_sticky_to_next_1-1.txt"))
    println("Done")
}

fun too_many_groups_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_too_many_groups.txt"))
    println("Done")
}
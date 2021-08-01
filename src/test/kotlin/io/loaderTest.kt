package io

import info.meodinger.lpfx.io.loadLP
import info.meodinger.lpfx.type.TransFile

import java.io.File

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: io
 */
lateinit var sample: TransFile

fun loaderTest() {
    sample = commonTest()
    comment_sticky_to_body_Test()
    empty_line_in_1_2_Test()
    empty_text_in_1_1_Test()
    empty_pic_in_1_Test()
    text_sticky_to_next_1_1Test()
    too_many_groups_Test()
    empty_group_name_Test()
    invalid_index_Test()
    repeated_group()
    repeated_index()
}

fun commonTest(): TransFile {
    return loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp.txt"))
}

fun comment_sticky_to_body_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_comment_sticky_to_body.txt"))
    println("Done: ${file.comment.trim() == sample.comment.trim()}")
}

fun empty_line_in_1_2_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_empty_line_in_1-2.txt"))
    println("Done: ${file.transMap["1.jpg"]!![1].text.indexOf("两个") == -1}")
}

fun empty_text_in_1_1_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_empty_text_in_1-1.txt"))
    println("Done: ${file.transMap["1.jpg"]!![0].text.isEmpty()}")
}

fun empty_pic_in_1_Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_empty_pic_in_1.txt"))
    println("Done: ${file.transMap["1.jpg"]!!.isEmpty()}")
}

fun text_sticky_to_next_1_1Test() {
    val file = loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_text_sticky_to_next_1-1.txt"))
    println("Done: ${file.transMap["1.jpg"]!![0] == sample.transMap["1.jpg"]!![0]}")
}

fun too_many_groups_Test() {
    try {
        loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_too_many_groups.txt"))
    } catch (e: Exception) {
        println("Done: ${e.message}")
    }
}

fun empty_group_name_Test() {
    try {
        loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_empty_group_name.txt"))
    } catch (e: Exception) {
        println("Done: ${e.message}")
    }
}

fun invalid_index_Test() {
    try {
        loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_invalid_index.txt"))
    } catch (e: Exception) {
        println("Done: ${e.message}")
    }
}

fun repeated_group() {
    try {
        loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_repeated_group.txt"))
    } catch (e: Exception) {
        println("Done: ${e.message}")
    }
}

fun repeated_index() {
    try {
        loadLP(File("D:\\WorkPlace\\Kotlin\\LabelPlusFX\\target\\sample\\lp_repeated_index.txt"))
    } catch (e: Exception) {
        println("Done: ${e.message}")
    }
}
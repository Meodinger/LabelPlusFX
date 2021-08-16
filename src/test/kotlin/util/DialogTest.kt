package util

import info.meodinger.lpfx.util.dialog.*

/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Location: util
 */
fun dialogIconTest() {
    showConfirm("Title", null, "Something to confirm")
    showInfo("Title", null, "Info you should know")
    showAlert("Title", null, "Alert you should take care of")
    showError("Title", null, "Error that the app occurs")
}

fun dialogHeaderTest() {
    showConfirm("title", "Test header", "confirm")
    showInfo("title", "Test header", "info")
    showAlert("title", "Test header", "alert")
    showError("title", "Test header", "error")
}

fun dialogExceptionTest() {
    try {
        val a = 0
        val b = 4
        b / a
    } catch (e: Exception) {
        showException(e)
    }
}

fun dialogLinkTest() {
    showLink(
        null,
        "title",
        "header",
        "content",
        "link"
    ) {
        println("handler")
    }
}

fun dialogTest() {
    dialogIconTest()
    dialogHeaderTest()
    dialogExceptionTest()
    dialogLinkTest()
}
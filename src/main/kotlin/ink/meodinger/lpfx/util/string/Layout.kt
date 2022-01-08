package ink.meodinger.lpfx.util.string

import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

/**
 * Omit high text
 */
fun omitHighText(longText: String, maxRowCount: Int = 10): String {
    val lines = longText.split("\n")

    if (lines.size <= maxRowCount) return longText

    val builder = StringBuilder()
    for (i in 0 until maxRowCount - 1) builder.appendLine(lines[i])
    builder.append(String.format(I18N["util.long_text.i"], lines.size - (maxRowCount - 1)))

    return builder.toString()
}

/**
 * Omit wide text
 */
fun omitWideText(longText: String, maxWidth: Double, font: Font? = null): String {
    val lines = longText.split("\n")
    val builder = StringBuilder()

    val t = Text().apply { this.font = font }
    val b = StringBuilder()
    var p: Int
    for (line in lines) {
        t.text = line
        b.clear()
        p = t.text.length - 1
        if (t.boundsInLocal.width > maxWidth) {
            b.append(line).append("...")
            while (t.boundsInLocal.width > maxWidth) {
                b.deleteAt(p--)
                t.text = b.toString()
            }
            builder.appendLine(b.toString())
        } else {
            builder.appendLine(line)
        }
    }
    if (builder.isNotEmpty()) builder.deleteTail("\n")

    return builder.toString()
}

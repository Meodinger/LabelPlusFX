package ink.meodinger.lpfx.util.string

import javafx.scene.text.Font
import javafx.scene.text.Text


/**
 * Author: Meodinger
 * Date: 2021/12/5
 * Have fun with my code!
 */

/**
 * Shorten long text
 */
fun shortenLongText(longText: String, maxRowCount: Int = 10): String {
    val lines = longText.split("\n")

    if (lines.size <= maxRowCount) return longText

    val builder = StringBuilder()
    for (i in 0 until maxRowCount - 1) builder.appendLine(lines[i])
    builder.append("... (+ ${lines.size - maxRowCount + 1})")

    return builder.toString()
}

/**
 * Shorten wide text
 */
fun shortenWideText(longText: String, maxWidth: Double, font: Font? = null): String {
    val lines = longText.lines()
    val textBuilder = StringBuilder()

    val text = Text().apply { this.font = font }
    val builder = StringBuilder()
    var pointer: Int
    for (line in lines) {
        text.text = line
        builder.clear()
        pointer = text.text.length - 1

        if (text.boundsInLocal.width > maxWidth) {
            text.text += "..."
            builder.append(line).append("...")

            // Fix: p should greater than 0 to hanldle "..." is wide enough case.
            while (pointer > 0 && text.boundsInLocal.width > maxWidth) {
                builder.deleteAt(pointer--)
                text.text = builder.toString()
            }
            textBuilder.appendLine(builder.toString())
        } else {
            textBuilder.appendLine(line)
        }
    }
    if (textBuilder.isNotEmpty()) textBuilder.deleteTrailingEOL()

    return textBuilder.toString()
}

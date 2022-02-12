package ink.meodinger.lpfx.component.singleton

import ink.meodinger.htmlparser.HNode
import ink.meodinger.htmlparser.parse
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.translator.translateJP

import javafx.beans.binding.Bindings
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import javax.net.ssl.HttpsURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Simple online dictionary, better than none, anyway.
 */
object AOnlineDict : Stage() {

    private const val JD_SITE: String = "https://nekodict.com"
    private const val JD_API: String = "https://nekodict.com/words?q="

    private val stateLabel = Label()
    private val inputField = TextField()
    private val outputArea = TextArea()

    private const val STATE_WORD     = 0
    private const val STATE_SENTENCE = 1
    private val stateProperty: IntegerProperty = SimpleIntegerProperty(STATE_WORD)
    private var state: Int by stateProperty

    init {
        icons.add(ICON)
        width = 300.0
        height = 200.0
        scene = Scene(BorderPane().apply {
            top(HBox()) {
                alignment = Pos.CENTER
                backgroundProperty().bind(Bindings.createObjectBinding({
                    Background(BackgroundFill(
                        when (state) {
                            STATE_WORD -> Color.LIGHTGREEN
                            STATE_SENTENCE -> Color.LIGHTBLUE
                            else -> throw IllegalStateException("State invalid")
                        },
                        CornerRadii(0.0),
                        Insets(0.0)
                    ))
                }, stateProperty))
                add(stateLabel) {
                    minWidth = 75.0
                    alignment = Pos.CENTER
                    textProperty().bind(Bindings.createStringBinding({
                        when (state) {
                            STATE_WORD -> "Word: "
                            STATE_SENTENCE -> "Sentence: "
                            else -> throw IllegalStateException("State invalid")
                        }
                    }, stateProperty))
                }
                add(inputField) {
                    boxHGrow = Priority.ALWAYS
                    addEventHandler(KeyEvent.KEY_PRESSED) {
                        if (it.code != KeyCode.TAB) return@addEventHandler
                        state = (state + 1) % 2
                        it.consume()
                    }
                    setOnAction {
                        outputArea.text = I18N["dict.fetching"]
                        when (state) {
                            STATE_WORD -> fetchInfo(text, outputArea::setText)
                            STATE_SENTENCE -> outputArea.text = translateJP(text)
                            else -> throw IllegalStateException("State invalid")
                        }
                    }
                }
            }
            center(outputArea) {
                isWrapText = true
                isEditable = false
            }
        })
    }

    private fun fetchInfoSync(word: String): String {
        val searchConnection = URL("$JD_API$word").openConnection().apply { connect() } as HttpsURLConnection
        if (searchConnection.responseCode != 200) return String.format(I18N["dict.search_error.i"], searchConnection.responseCode)

        val searchHTML = searchConnection.inputStream.reader(StandardCharsets.UTF_8).readText()
        val searchResults = parse(searchHTML).body.children[1].children[2]
        val first = searchResults.children[0]
        if (first.attributes["id"] == "out-search") return I18N["dict.not_found"]

        val target = JD_SITE + first.attributes["href"]
        val contentConnection = URL(target).openConnection().apply { connect() } as HttpsURLConnection
        if (contentConnection.responseCode != 200) return String.format(I18N["dict.search_error.i"], contentConnection.responseCode)

        // ContentHTML has syntax error
        val contentHTML = contentConnection.inputStream.reader(StandardCharsets.UTF_8).readText().replace("</body>", "</div></body>")
        val contentResults = parse(contentHTML).body.children[1].children[1].children[0]

        return contentResults.children
            .filter { it.attributes.getOrDefault("class", "").startsWith("my-4 p-3") }
            .joinToString("\n\n") { (it.children[0].children[0] as HNode.HText).text }
    }

    private fun fetchInfo(word: String, callback: (String) -> Unit) {
        LPFXTask.createTask<String> { fetchInfoSync(word) }.apply {
            setOnSucceeded(callback)
            setOnFailed { callback(it.toString()) }
        }()
    }

}

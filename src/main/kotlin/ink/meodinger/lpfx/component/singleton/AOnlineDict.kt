package ink.meodinger.lpfx.component.singleton

import ink.meodinger.htmlparser.HNode
import ink.meodinger.htmlparser.parse
import ink.meodinger.lpfx.DIALOG_HEIGHT
import ink.meodinger.lpfx.DIALOG_WIDTH
import ink.meodinger.lpfx.util.Promise
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.setValue

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


object AOnlineDict : Stage() {

    private const val JD_SITE: String = "https://nekodict.com"
    private const val JD_API: String = "https://nekodict.com/words?q="
    private const val BD_API: String = ""

    private val stateLabel = Label()
    private val inputField = TextField()
    private val outputArea = TextArea()

    private const val STATE_WORD     = 0
    private const val STATE_SENTENCE = 1
    private val stateProperty: IntegerProperty = SimpleIntegerProperty(STATE_WORD)
    private var state: Int by stateProperty

    init {
        width = DIALOG_WIDTH
        height = DIALOG_HEIGHT
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
                    hGrow = Priority.ALWAYS
                    addEventHandler(KeyEvent.KEY_PRESSED) {
                        if (it.code != KeyCode.TAB) return@addEventHandler
                        state = (state + 1) % 2
                        it.consume()
                    }
                    setOnAction {
                        outputArea.text = "Fetching..."
                        fetchInfo(text, outputArea::setText)
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
        val searchConnection = URL("$JD_API$word").openConnection() as HttpsURLConnection
        searchConnection.connectTimeout = 10000
        searchConnection.connect()
        if (searchConnection.responseCode != 200) return "Error when search"

        val searchHTML = searchConnection.inputStream.reader(StandardCharsets.UTF_8).readText()
        val searchResults = parse(searchHTML).body.children[1].children[2]
        val first = searchResults.children[0]
        if (first.attributes["id"] == "out-search") return "Not Found"

        val target = JD_SITE + first.attributes["href"]
        val contentConnection = URL(target).openConnection() as HttpsURLConnection
        contentConnection.connectTimeout = 10000
        contentConnection.connect()
        if (contentConnection.responseCode != 200) return "Error when fetch"

        // ContentHTML has syntax error
        val contentHTML = contentConnection.inputStream.reader(StandardCharsets.UTF_8).readText().replace("</body>", "</div></body>")
        val contentResults = parse(contentHTML).body.children[1].children[1].children[0]
        val content = contentResults.children
            .filter { it.attributes.getOrDefault("class", "").startsWith("my-4 p-3") }
            .joinToString("\n\n") { (it.children[0].children[0] as HNode.HText).text }

        return content
    }
    private fun fetchInfo(word: String, callback: (String) -> Unit) {
        Promise<String> { re, _ ->
            re(fetchInfoSync(word))
        } then {
            callback(it)
        } catch { it: Throwable ->
            it.also { callback(it.toString()) }
        }
    }

}

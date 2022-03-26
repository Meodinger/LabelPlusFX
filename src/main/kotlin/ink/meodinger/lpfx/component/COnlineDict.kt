package ink.meodinger.lpfx.component

import ink.meodinger.htmlparser.HNode
import ink.meodinger.htmlparser.parse
import ink.meodinger.lpfx.COMMON_GAP
import ink.meodinger.lpfx.LOGSRC_DICTIONARY
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.ime.*
import ink.meodinger.lpfx.util.property.getValue
import ink.meodinger.lpfx.util.property.onNew
import ink.meodinger.lpfx.util.property.setValue
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.emptyString
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
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.Window
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.HttpsURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.jvm.Throws


/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Simple online dictionary, better than none, anyway.
 */
class COnlineDict : Stage() {

    companion object {
        private const val JD_SITE        = "https://nekodict.com"
        private const val JD_API         = "https://nekodict.com/words?q="

        private const val STATE_WORD     = 0
        private const val STATE_SENTENCE = 1
    }

    private val transStateProperty: IntegerProperty = SimpleIntegerProperty(STATE_WORD)
    private var transState: Int by transStateProperty

    private var outputArea: TextArea = TextArea()
    private var oriLang: String = emptyString()

    init {
        icons.add(ICON)
        title = "${INFO["application.name"]} - Dict"
        width = 300.0
        height = 200.0
        scene = Scene(BorderPane().apply {
            top(HBox()) {
                alignment = Pos.CENTER
                backgroundProperty().bind(Bindings.createObjectBinding({
                    Background(BackgroundFill(
                        when (transState) {
                            STATE_WORD -> Color.LIGHTGREEN
                            STATE_SENTENCE -> Color.LIGHTBLUE
                            else -> throw IllegalStateException("State invalid")
                        },
                        CornerRadii(0.0),
                        Insets(0.0)
                    ))
                }, transStateProperty))
                add(Label()) {
                    minWidth = 75.0
                    alignment = Pos.CENTER
                    textProperty().bind(Bindings.createStringBinding({
                        when (transState) {
                            STATE_WORD -> "Word: "
                            STATE_SENTENCE -> "Sentence: "
                            else -> throw IllegalStateException("State invalid")
                        }
                    }, transStateProperty))
                }
                add(TextField()) {
                    boxHGrow = Priority.ALWAYS
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code != KeyCode.TAB) return@addEventFilter
                        transState = (transState + 1) % 2
                        it.consume()
                    }
                    addEventHandler(MouseEvent.MOUSE_CLICKED) {
                        if (it.isDoubleClick && getCurrentLanguage().startsWith(JA)) {
                            setImeConversionMode(
                                getWindowHandle(this@COnlineDict),
                                ImeSentenceMode.AUTOMATIC,
                                ImeConversionMode.JA_HIRAGANA
                            )
                        }
                    }
                    setOnAction {
                        outputArea.text = I18N["dict.fetching"]
                        when (transState) {
                            STATE_WORD     -> fetchInfo(text, outputArea::setText)
                            STATE_SENTENCE -> outputArea.text = translateJP(text)
                            else -> throw IllegalStateException("State invalid")
                        }
                    }
                }
            }
            center(outputArea) {
                isWrapText = true
                isEditable = false
                font = font.s(16.0)
            }
        })

        focusedProperty().addListener(onNew {
            if (it) {
                oriLang = getCurrentLanguage()
                languages.firstOrNull { lang -> lang.startsWith(JA) }?.apply(::setCurrentLanguage)
            } else {
                setCurrentLanguage(oriLang)
            }
        })
    }

    @Throws(IOException::class, SocketTimeoutException::class)
    private fun fetchInfoSync(word: String): String {
        val searchConnection = URL("$JD_API$word").openConnection().apply { connect() } as HttpsURLConnection
        if (searchConnection.responseCode != 200) return String.format(I18N["dict.search_error.i"], searchConnection.responseCode)

        val searchHTML = searchConnection.inputStream.reader(StandardCharsets.UTF_8).readText()
        val searchResults = parse(searchHTML).body.children[1].children[2]
        val first = searchResults.children.getOrNull(0)
        if (first == null || first.attributes["id"] == "out-search") return I18N["dict.not_found"]

        val target = JD_SITE + first.attributes["href"]
        val contentConnection = URL(target).openConnection().apply { connect() } as HttpsURLConnection
        if (contentConnection.responseCode != 200) return String.format(I18N["dict.search_error.i"], contentConnection.responseCode)

        // ContentHTML has unclosed div
        val contentHTML = contentConnection.inputStream.reader(StandardCharsets.UTF_8).readText().replace("</body>", "</div></body>")
        val contentResults = parse(contentHTML).body.children[1].children[1].children[0].children

        // Build WordInfo
        fun HNode.isWordExplanation() = attributes["class"]?.startsWith("my-4 p-3") ?: false
        fun HNode.isWordSentence() = attributes["class"]?.startsWith("sentence-block") ?: false
        fun HNode.jpText(): String {
            val builder = StringBuilder()
            for (node in children) {
                if (node is HNode.HText) builder.append(node.text)
                if (node.nodeType == "ruby") for (n in node.children) if (n is HNode.HText) builder.append(n.text)
            }
            return builder.toString()
        }

        var nodeIndex = -1
        var wordIndex = 1
        val infoBuilder = StringBuilder()

        infoBuilder.append(word).append(" : ").append((contentResults[2].children[0].children[0] as HNode.HText).text).append("\n\n")
        while (++nodeIndex < contentResults.size) {
            val node = contentResults[nodeIndex]
            if (!node.isWordExplanation()) continue

            infoBuilder.append("<").append(wordIndex++).append("> ").appendLine((node.children[0].children[0] as HNode.HText).text)
            while ((nodeIndex + 1) < contentResults.size && contentResults[nodeIndex + 1].isWordSentence()) {
                val sentenceNode = contentResults[++nodeIndex].children[0].children[0]
                infoBuilder.append(" - ").appendLine(sentenceNode.children[0].jpText().trim())
                infoBuilder.append(" > ").appendLine((sentenceNode.children[1].children[0] as HNode.HText).text.trim())
            }
            infoBuilder.appendLine()
        }

        return infoBuilder.toString()
    }

    private fun fetchInfo(word: String, callback: (String) -> Unit) {
        LPFXTask.createTask<String> { fetchInfoSync(word) }.apply {
            setOnSucceeded {
                Logger.info("Fetched word info: $word", LOGSRC_DICTIONARY)
                callback(it)
            }
            setOnFailed {
                Logger.error("Fetch word info failed", LOGSRC_DICTIONARY)
                Logger.exception(it)
                callback(it.toString())
            }
        }()
    }

    fun showDict(relativeTo: Window?) {
        if (relativeTo != null) {
            x = relativeTo.x - (width + COMMON_GAP * 2) + relativeTo.width
            y = relativeTo.y + (COMMON_GAP * 2)
        } else {
            centerOnScreen()
        }
        show()
    }

}

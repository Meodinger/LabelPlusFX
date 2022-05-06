package ink.meodinger.lpfx.component.tools

import ink.meodinger.htmlparser.HNode
import ink.meodinger.htmlparser.parse
import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CTextFlow
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.ime.*
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.translator.translateJP

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection


/**
 * Author: Meodinger
 * Date: 2022/1/17
 * Have fun with my code!
 */

/**
 * Simple online dictionary, better than none, anyway.
 */
class OnlineDict : Stage() {

    companion object {
        private const val JD_SITE = "https://nekodict.com"
        private const val JD_API  = "https://nekodict.com/words?q="

        private const val FONT_SIZE = 16.0

        private fun HNode.isWordExplanation() = attributes["class"]?.startsWith("my-4 p-3") ?: false
        private fun HNode.isWordSentence() = attributes["class"]?.startsWith("sentence-block") ?: false
        private fun HNode.jpText(): String {
            val builder = StringBuilder()
            for (node in children) {
                if (node is HNode.HText) builder.append(node.text)
                if (node.nodeType == "ruby") for (n in node.children) if (n is HNode.HText) builder.append(n.text)
            }
            return builder.toString()
        }
    }

    private enum class TransState { WORD, SENTENCE; }

    private val transStateProperty: ObjectProperty<TransState> = SimpleObjectProperty(TransState.WORD)
    private var transState: TransState by transStateProperty

    private val outputFlow: CTextFlow = CTextFlow()

    private var oriLang: String = emptyString()

    init {
        icons.add(ICON)
        title = "${INFO["application.name"]} - Dict"
        width = 300.0
        height = 200.0
        scene = Scene(BorderPane().apply {
            top(HBox()) {
                alignment = Pos.CENTER
                backgroundProperty().bind(transStateProperty.transform {
                    Background(BackgroundFill(
                        when (it!!) {
                            TransState.WORD -> Color.LIGHTGREEN
                            TransState.SENTENCE -> Color.LIGHTBLUE
                        },
                        CornerRadii(0.0),
                        Insets(0.0)
                    ))
                })
                add(Label()) {
                    minWidth = 75.0
                    alignment = Pos.CENTER
                    textProperty().bind(transStateProperty.transform {
                        when (it!!) {
                            TransState.WORD -> I18N["dict.word"]
                            TransState.SENTENCE -> I18N["dict.sentence"]
                        }
                    })
                }
                add(TextField()) {
                    hgrow = Priority.ALWAYS
                    addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code != KeyCode.TAB) return@addEventFilter
                        transState = TransState.values()[(transState.ordinal + 1) % TransState.values().size]
                        it.consume()
                    }
                    addEventHandler(MouseEvent.MOUSE_CLICKED) {
                        if (it.isDoubleClick && getCurrentLanguage().startsWith(JA)) {
                            setImeConversionMode(
                                getCurrentWindow(),
                                ImeSentenceMode.AUTOMATIC,
                                ImeConversionMode.JA_HIRAGANA
                            )
                        }
                    }
                    setOnAction {
                        outputFlow.setText(I18N["dict.fetching"], FONT_SIZE)
                        outputFlow.flow()
                        when (transState) {
                            TransState.WORD -> searchWord(text)
                            TransState.SENTENCE -> translate(text)
                        }
                    }
                }
            }
            center(ScrollPane()) scroll@{
                withContent(outputFlow) {
                    isInstant = false
                    fontSize = 16.0

                    padding = Insets(16.0, 0.0, 16.0, 16.0)
                    prefWidthProperty().bind(this@scroll.widthProperty() - 16.0)
                }
            }
        })

        focusedProperty().addListener(onNew {
            // We do not set the IMEConversion mode here because switch language need time.
            if (it) {
                oriLang = getCurrentLanguage()
                // Focus gain will take place after the rendering, so it's safe to set by sync.
                availableLanguages.firstOrNull { lang -> lang.startsWith(JA) }?.apply(::setCurrentLanguage)
            } else {
                // If set immediately after lose focus will cause focus on other stages fail.
                // Use runLater to set language after the rendering.
                Platform.runLater { setCurrentLanguage(oriLang) }
            }
        })
        closeOnEscape()
    }

    private fun searchWordSync(word: String) {
        val searchConnection = URL("$JD_API$word").openConnection().apply { connect() } as HttpsURLConnection
        Logger.debug("Dictionary: Fetching URL ${searchConnection.url}", "Dictionary")
        if (searchConnection.responseCode != 200) {
            Logger.debug(searchConnection.errorStream.reader(StandardCharsets.UTF_8).readText(), "Dictionary")
            outputFlow.setText(String.format(I18N["dict.search_error.i"], searchConnection.responseCode))
            return
        }

        val searchHTML = searchConnection.inputStream.reader(StandardCharsets.UTF_8).readText().also {
            Logger.debug("Dictionary: Got HTML content:", "Dictionary")
            Logger.debug(it, "Dictionary")
        }
        val searchPage = parse(searchHTML)
        val searchResults = searchPage.body.children[1].children[3]

        // Look for detail information
        val first = searchResults.children.getOrNull(0)
        if (first == null || first.attributes["id"] == "out-search") {
            outputFlow.setText(I18N["dict.not_found"])
            return
        }

        val contentConnection = URL(JD_SITE + first.attributes["href"]).openConnection().apply { connect() } as HttpsURLConnection
        Logger.debug("Dictionary: Fetching URL ${contentConnection.url}", "Dictionary")
        if (contentConnection.responseCode != 200){
            Logger.debug(contentConnection.errorStream.reader(StandardCharsets.UTF_8).readText(), "Dictionary")
            outputFlow.setText(String.format(I18N["dict.search_error.i"], searchConnection.responseCode))
            return
        }

        val contentHTML = contentConnection.inputStream.reader(StandardCharsets.UTF_8).readText().also {
            Logger.debug("Dictionary: Got HTML content:", "Dictionary")
            Logger.debug(it, "Dictionary")
        }
        // ContentHTML has unclosed div
        val contentPage = parse(contentHTML.replace("</body>", "</div></body>"))
        val contentResults = contentPage.body.children[1].children[1].children[0].children

        // Build WordInfo
        var nodeIndex = 0
        var explanationIndex = 1

        outputFlow.clear()
        outputFlow.appendLine("$word : ${(contentResults[2].children[0].children[0] as HNode.HText).text}", bold = true)
        while (nodeIndex < contentResults.size) {
            val node = contentResults[nodeIndex++]
            if (!node.isWordExplanation()) continue

            outputFlow.appendLine()
            outputFlow.appendLine("<${explanationIndex++}> ${(node.children[0].children[0] as HNode.HText).text}")
            while (nodeIndex < contentResults.size && contentResults[nodeIndex].isWordSentence()) {
                val sentenceNode = contentResults[nodeIndex].children[0].children[0]
                outputFlow.appendLine("  ${sentenceNode.children[0].jpText().trim()}", color = Color.RED)
                outputFlow.appendLine("    ${(sentenceNode.children[1].children[0] as HNode.HText).text.trim()}", color = Color.BLUE)
                nodeIndex++
            }
        }
    }
    private fun searchWord(word: String) {
        LPFXTask.createTask<Unit> { searchWordSync(word) }.apply {
            setOnSucceeded {
                Logger.info("Dictionary: Fetched word info: $word", "Dictionary")
                Platform.runLater { outputFlow.flow() }
            }
            setOnFailed {
                Logger.error("Dictionary: Fetch word info failed", "Dictionary")
                Logger.exception(it)
                Platform.runLater { outputFlow.flow() }
                showException(null, it)
            }
        }() // Remember to invoke
    }

    private fun translateSync(text: String) {
        outputFlow.setText(translateJP(text))
    }
    private fun translate(text: String) {
        LPFXTask.createTask<Unit> { translateSync(text) }.apply {
            setOnSucceeded {
                Logger.info("Dictionary: Fetched translation", "Dictionary")
                Platform.runLater { outputFlow.flow() }
            }
            setOnFailed {
                Logger.error("Dictionary: Fetch translation failed", "Dictionary")
                Logger.exception(it)
                Platform.runLater { outputFlow.flow() }
                showException(null, it)
            }
        }() // Remember to invoke
    }

}

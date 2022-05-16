package ink.meodinger.lpfx.component.tools

import ink.meodinger.lpfx.*
import ink.meodinger.lpfx.component.common.CTextFlow
import ink.meodinger.lpfx.component.dialog.showException
import ink.meodinger.lpfx.ime.*
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.*
import ink.meodinger.lpfx.util.event.isDoubleClick
import ink.meodinger.lpfx.util.property.*
import ink.meodinger.lpfx.util.string.emptyString
import ink.meodinger.lpfx.util.string.remove
import ink.meodinger.lpfx.util.translator.translateJP

import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
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
import java.net.URLEncoder
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
        // Maybe: Take back Neko-Dict
        // private const val NEKO_SITE = "https://nekodict.com"
        // private const val NEKO_API  = "https://nekodict.com/words?q="

        private const val WEBLIO_API  = "https://www.weblio.jp/content/"

        private const val FONT_SIZE = 16.0
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
                        // Mark immediately when this event will be consumed
                        it.consume() // disable further propagation

                        transState = TransState.values()[(transState.ordinal + 1) % TransState.values().size]
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
                            TransState.WORD -> searchWeblio(text)
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

    private fun searchWeblioSync(word: String) {
        // URL encode the word to make sure we search the correct thing
        val weblioURL = WEBLIO_API + URLEncoder.encode(word, StandardCharsets.UTF_8)
        Logger.debug("Dictionary: Fetching URL $weblioURL", "Dictionary")
        val weblioConnection = URL(weblioURL).openConnection().apply { connect() } as HttpsURLConnection
        if (weblioConnection.responseCode != 200) {
            Logger.debug(weblioConnection.errorStream.reader(StandardCharsets.UTF_8).readText(), "Dictionary")
            outputFlow.setText(String.format(I18N["dict.search_error.i"], weblioConnection.responseCode))
            return
        }
        val weblioHTML = weblioConnection.inputStream.reader(StandardCharsets.UTF_8).readText()
        val weblioPage = Jsoup.parse(weblioHTML)

        val notfound = weblioPage.selectXpath("//div[@id=\"nrCntTH\"]/p/text()")
        if (notfound.isNotEmpty()) {
            outputFlow.clear()
            outputFlow.appendLine(notfound[0].text())
            return
        }

        // to remove
        val regexDocumentWrtie = Regex("(document.write\\()(.*)(\\);)") // In-dom js
        val regexUselessSource = Regex("(\u51fa\u5178)(.*)(\\))") // Useless Souce
        val regexKanjiCafeSource = Regex("(\u203b\u3054\u5229\u7528)(.*)(Cafe.)") // Kanji Cafe Source
        // to replace
        val regexNumber = Regex("[\uFF10-\uFF19]+ ") // Full-Width 0-9
        val regexEnglish = Regex(" [a-zA-Z]+ ") // English with trailing whitespace
        val regexEnglishDot = Regex("[a-zA-Z]+, ") // English with trailing dot
        val regexWhitespace = Regex("( )+") // Multi whitespace
        val regexMultiNewLine = Regex("(\n)+") // Multi new line

        val sourceList = weblioPage.selectXpath("//div[@class=\"pbarTL\"]").map { it.wholeText().trim() }
        val definationList = weblioPage.selectXpath("//div[@class=\"kiji\"]").map {
            // Example sentences
            if (it.children()[1].hasClass("Wnryj")) {
                return@map it.children()[1].children()[0].children().mapIndexed { index, element ->
                    "${index + 1}.${element.wholeText()}"
                }.joinToString("\n\u3000")
            }

            it.selectXpath("//br").forEach { element -> element.replaceWith(TextNode(" ")) }
            var text = it.wholeText()
                .remove(regexDocumentWrtie)
                .remove(regexUselessSource)
                .remove(regexKanjiCafeSource)

            regexNumber.findAll(text).toList().reversed().forEach { result ->
                text = text.replaceRange(result.range, result.value.dropLast(1).plus("."))
            }
            regexEnglish.findAll(text).toList().reversed().forEach { result ->
                text = text.replaceRange(result.range, result.value.drop(1).dropLast(1))
            }
            regexEnglishDot.findAll(text).toList().reversed().forEach { result ->
                text = text.replaceRange(result.range, result.value.dropLast(1))
            }

            text.replace(regexWhitespace, "\n").replace(regexMultiNewLine, "\n\u3000").trim()
        }

        outputFlow.clear()
        for ((source, defination) in sourceList.zip(definationList)) {
            outputFlow.appendLine(source, bold = true)
            outputFlow.appendText("\u3000$defination\n\n")
        }
    }
    private fun searchWeblio(word: String) {
        LPFXTask.createTask<Unit> { searchWeblioSync(word) }.apply {
            setOnSucceeded {
                Logger.info("Dictionary: Fetched weblio info: $word", "Dictionary")
                Platform.runLater { outputFlow.flow() }
            }
            setOnFailed {
                Logger.error("Dictionary: Fetch weblio info failed", "Dictionary")
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

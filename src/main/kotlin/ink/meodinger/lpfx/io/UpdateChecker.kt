package ink.meodinger.lpfx.io

import com.fasterxml.jackson.databind.ObjectMapper
import ink.meodinger.lpfx.COMMON_GAP
import ink.meodinger.lpfx.LOGSRC_CHECKER
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.V
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.Version
import ink.meodinger.lpfx.util.component.add
import ink.meodinger.lpfx.util.component.withContent
import ink.meodinger.lpfx.util.dialog.infoImageView
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.VBox
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.util.Date


/**
 * Author: Meodinger
 * Date: 2021/12/23
 * Have fun with my code!
 */

object UpdateChecker {

    private const val API: String      = "https://api.github.com/repos/Meodinger/LabelPlusFX/releases"
    private const val RELEASES: String = "https://github.com/Meodinger/LabelPlusFX/releases"
    private const val DELAY: Long      = 604800L

    fun fetchSync(): Version {
        try {
            val jsonNode = ObjectMapper().readTree(URL(API))
            if (jsonNode.isArray) return Version.of(jsonNode[0]["name"].asText())
        } catch (e: NoRouteToHostException) {
            Logger.warning("No network connection", LOGSRC_CHECKER)
        } catch (e: SocketTimeoutException) {
            Logger.warning("Connect timeout", LOGSRC_CHECKER)
        } catch (e: ConnectException) {
            Logger.warning("Connect failed", LOGSRC_CHECKER)
        } catch (e: IOException) {
            Logger.warning("Fetch I/O failed", LOGSRC_CHECKER)
            Logger.exception(e)
        }
        return Version.V0
    }

    /**
     * Start a new LPFX task to check and show update info
     * Should run after stage showed
     */
    fun check() {
        LPFXTask {
            Logger.info("Fetching latest version...", LOGSRC_CHECKER)
            val version = fetchSync()
            if (version != Version.V0) Logger.info("Got latest version: $version", LOGSRC_CHECKER)

            if (version > V) {
                val time = Date().time
                val last = Preference[Preference.LAST_UPDATE_NOTICE].asLong()
                if (last != 0L && time - last < DELAY) {
                    Logger.info("Check suppressed, last notice time is $last", LOGSRC_CHECKER)
                    return@LPFXTask
                }

                Preference[Preference.LAST_UPDATE_NOTICE] = 0
                Platform.runLater {
                    val suppressNoticeButtonType = ButtonType(I18N["update.dialog.suppress"], ButtonBar.ButtonData.OK_DONE)

                    val dialog = Dialog<ButtonType>()
                    dialog.initOwner(State.stage)
                    dialog.title = I18N["update.dialog.title"]
                    dialog.graphic = infoImageView
                    dialog.dialogPane.buttonTypes.addAll(suppressNoticeButtonType, ButtonType.CLOSE)
                    dialog.withContent(VBox()) {
                        add(Label(String.format(I18N["update.dialog.content.s"], version)))
                        add(Separator()) {
                            padding = Insets(COMMON_GAP / 2, 0.0, COMMON_GAP / 2, 0.0)
                        }
                        add(Hyperlink(I18N["update.dialog.link"])) {
                            padding = Insets(0.0)
                            setOnAction { State.application.hostServices.showDocument(RELEASES) }
                        }
                    }

                    val suppressButton = dialog.dialogPane.lookupButton(suppressNoticeButtonType)
                    ButtonBar.setButtonUniformSize(suppressButton, false)

                    dialog.showAndWait().ifPresent {
                        if (it == suppressNoticeButtonType) {
                            Preference[Preference.LAST_UPDATE_NOTICE] = time
                            Logger.info("Check suppressed, next notice time is ${time + DELAY}", LOGSRC_CHECKER)
                        }
                    }
                }
            }
        }()
    }

}

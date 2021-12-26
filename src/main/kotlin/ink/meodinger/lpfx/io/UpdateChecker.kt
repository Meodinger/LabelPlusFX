package ink.meodinger.lpfx.io

import com.fasterxml.jackson.databind.ObjectMapper
import ink.meodinger.lpfx.LOGSRC_CHECKER
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.component.add
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
import java.util.*
import java.util.regex.Pattern


/**
 * Author: Meodinger
 * Date: 2021/12/23
 * Have fun with my code!
 */

object UpdateChecker {

    private const val API: String      = "https://api.github.com/repos/Meodinger/LabelPlusFX/releases"
    private const val RELEASES: String = "https://github.com/Meodinger/LabelPlusFX/releases"
    private const val DELAY: Long      = 604800L

    val V = Version(2, 2, 2)

    data class Version(val a: Int, val b: Int, val c: Int): Comparable<Version> {

        companion object {
            val V0 = Version(0, 0, 0)

            private val pattern = Pattern.compile("(v)?[0-9].[0-9].[0-9]", Pattern.CASE_INSENSITIVE)
            private fun check(i : Int): Int {
                if (i !in 0..99)
                    throw IllegalArgumentException("Version number must in 0..99, got $i")
                return i
            }

            fun of(version: String): Version {
                if (!pattern.matcher(version).matches()) return V0

                val l = version.split(".")
                val hasPrefix = version.startsWith("v", true)

                val a = check(l[0].substring(if (hasPrefix) 1 else 0).toInt())
                val b = check(l[1].toInt())
                val c = check(l[2].toInt())
                return Version(a, b, c)
            }
        }

        init {
            check(a)
            check(b)
            check(c)
        }

        override fun toString(): String = "v$a.$b.$c"

        override operator fun compareTo(other: Version): Int {
            return (this.c - other.c) + (this.b - other.b) * 100 + (this.a - other.a) * 10000
        }

    }

    fun fetchSync(): Version {
        try {
            val jsonNode = ObjectMapper().readTree(URL(API))
            if (jsonNode.isArray) return Version.of(jsonNode[0]["name"].asText())
        } catch (e: NoRouteToHostException) {
            Logger.warning("No network connection, check failed", LOGSRC_CHECKER)
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

            if (version < V) {
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
                    dialog.dialogPane.content = VBox().apply {
                        add(Label(String.format(I18N["update.dialog.content.s"], version)))
                        add(Separator()) {
                            padding = Insets(8.0, 0.0, 8.0, 0.0)
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
        }.startInNewThread()
    }

}

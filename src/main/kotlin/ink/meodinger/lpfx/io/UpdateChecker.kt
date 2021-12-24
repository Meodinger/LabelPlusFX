package ink.meodinger.lpfx.io

import com.fasterxml.jackson.databind.ObjectMapper
import ink.meodinger.lpfx.LOGSRC_CHECKER
import ink.meodinger.lpfx.State
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.type.LPFXTask
import ink.meodinger.lpfx.util.Promise
import ink.meodinger.lpfx.util.dialog.infoImageView
import ink.meodinger.lpfx.util.dialog.showLink
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get

import javafx.application.Platform
import java.io.IOException
import java.net.MalformedURLException
import java.net.NoRouteToHostException
import java.net.URL


/**
 * Author: Meodinger
 * Date: 2021/12/23
 * Have fun with my code!
 */

object UpdateChecker {

    private const val API: String = "https://api.github.com/repos/Meodinger/LabelPlusFX/releases"
    private const val RELEASES: String = "https://github.com/Meodinger/LabelPlusFX/releases"
    private val V0 = Version(0, 0, 0)
    private val V = Version(2, 2, 2)

    data class Version(val a: Int, val b: Int, val c: Int): Comparable<Version> {

        companion object {
            fun of(version: String): Version? {
                if (!version.matches(Regex("[vV]?[0-9].[0-9].[0-9]"))) return null
                val l = version.split(".")
                return Version(l[0].substring(1).toInt(), l[1].toInt(), l[2].toInt())
            }
        }

        override fun toString(): String = "v$a.$b.$c"

        override operator fun compareTo(other: Version): Int {
            return (this.c - other.c) + (this.b - other.b) * 100 + (this.a - other.a) * 10000
        }

    }

    fun fetchSync(): Version {
        try {
            val jsonNode = ObjectMapper().readTree(URL(API))
            if (jsonNode.isArray) return Version.of(jsonNode[0]["name"].asText()) ?: V0
        } catch (e: MalformedURLException) {
            Logger.error("Malformed fetch api url", LOGSRC_CHECKER)
            Logger.exception(e)
        } catch (e: NoRouteToHostException) {
            Logger.warning("No network connection, check failed", LOGSRC_CHECKER)
        } catch (e: IOException) {
            Logger.error("Fetch I/O failed", LOGSRC_CHECKER)
            Logger.exception(e)
        }
        return V0
    }

    /**
     * Start a new LPFX task to check and show update info
     * Should run after stage showed
     */
    fun check() {
        LPFXTask {
            Logger.info("Fetching latest version...", LOGSRC_CHECKER)
            val version = fetchSync()
            if (version != V0) Logger.info("Got latest version: $version", LOGSRC_CHECKER)

            if (version > V) Platform.runLater {
                showLink(
                    State.stage,
                    infoImageView,
                    I18N["update.dialog.title"],
                    null,
                    String.format(I18N["update.dialog.content.s"], version),
                    I18N["update.dialog.link"],
                ) {
                    State.application.hostServices.showDocument(RELEASES)
                }
            }
        }.startInNewThread()
    }

}

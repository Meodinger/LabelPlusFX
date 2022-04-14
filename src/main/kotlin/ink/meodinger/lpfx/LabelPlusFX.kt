package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.properties.DialogLogs
import ink.meodinger.lpfx.component.properties.DialogSettings
import ink.meodinger.lpfx.component.tools.*
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.HookedApplication
import ink.meodinger.lpfx.util.component.withOwner
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.property.onChange

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.MenuItem as AwtMenuItem
import java.awt.PopupMenu as AwtPopupMenu


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * LPFX Application
 */
class LabelPlusFX: HookedApplication() {

    companion object {
        const val PARAM_UNNAMED_NO_CHECK_UPDATE = "--no-check-update"
    }

    private val state: State = State()
    private val icon: TrayIcon by lazy {
        TrayIcon(Toolkit.getDefaultToolkit().getImage(loadAsURL("/file/image/icon.png"))).apply {
            fun restore() {
                Platform.runLater {
                    state.stage.show()
                    state.stage.toFront()
                    SystemTray.getSystemTray().remove(this)
                }
            }
            fun destroy() {
                Platform.runLater {
                    if (!state.isOpened || !state.isChanged)
                        this@LabelPlusFX.exit()
                }
            }

            isImageAutoSize = true
            popupMenu = AwtPopupMenu().apply {
                // AWT has problem with unicode characters display
                add(AwtMenuItem("Show").apply {
                    addActionListener { restore() }
                })
                addSeparator()
                add(AwtMenuItem("Exit").apply {
                    addActionListener { destroy() }
                })
            }
            addActionListener { restore() }
        }
    }

    val cheatSheet       by lazy {
        CheatSheet().apply {
            setOnAction { state.application.hostServices.showDocument(INFO["application.help"]) }
        } withOwner state.stage
    }
    val onlineDict       by lazy {
        OnlineDict() withOwner state.stage
    }
    val searchAndReplace by lazy {
        SearchReplace(state) withOwner state.stage
    }
    val textChecker      by lazy {
        TextChecker(state) withOwner state.stage
    }
    val dialogSpecify    by lazy {
        SpecifyFiles(state) withOwner state.stage
    }
    val dialogLogs       by lazy {
        DialogLogs() withOwner state.stage
    }
    val dialogSettings   by lazy {
        DialogSettings() withOwner state.stage
    }

    init {
        Logger.tic()

        Logger.info("App initializing...", LOGSRC_APPLICATION)

        state.application = this

        Options.load()

        Logger.info("App initialized", LOGSRC_APPLICATION)
    }

    override fun start(primaryStage: Stage) {
        Logger.info("App starting...", LOGSRC_APPLICATION)

        Thread.currentThread().setUncaughtExceptionHandler { t, e ->
            Logger.error("Exception uncaught in Thread: ${t.name}", LOGSRC_APPLICATION)
            Logger.exception(e)
            showException(primaryStage, e)
        }

        state.stage = primaryStage

        val root: View
        val controller: Controller
        try {
            root = View(state)
            controller = Controller(state)
        } catch (e: Throwable) {
            Logger.exception(e)
            showException(null, e)
            stop()
            return
        }

        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, Preference.windowWidth, Preference.windowHeight)
        primaryStage.setOnCloseRequest { if (!controller.stay()) exit() else it.consume() }

        // Window Size Listener
        val windowSizeListener: ChangeListener<Number> = onChange {
            if (primaryStage.isMaximized) return@onChange
            Preference.windowWidth = primaryStage.scene.width
            Preference.windowHeight = primaryStage.scene.height
        }
        primaryStage.scene.widthProperty().addListener(windowSizeListener)
        primaryStage.scene.heightProperty().addListener(windowSizeListener)

        // BOSS key
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) iconify()
        }

        primaryStage.show()

        Logger.info("App started", LOGSRC_APPLICATION)

        if (!parameters.unnamed.contains(PARAM_UNNAMED_NO_CHECK_UPDATE))
            if (Settings.autoCheckUpdate)
                controller.checkUpdate()

        Logger.toc()
    }

    override fun exit() {
        Logger.info("App stopping...", LOGSRC_APPLICATION)

        state.stage.close()
        Options.save()

        runHooks(
            {
                Logger.info("App stopped", LOGSRC_APPLICATION)
                Platform.exit()
            },
            {
                Logger.error("Exception occurred during hooks run", LOGSRC_APPLICATION)
                Logger.exception(it)
            }
        )
    }

    fun iconify() {
        if (Config.supportSysTray) {
            state.stage.hide()

            SystemTray.getSystemTray().add(icon)
        } else {
            state.stage.isIconified = true
        }
    }
}

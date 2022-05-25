package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.dialog.showException
import ink.meodinger.lpfx.component.properties.*
import ink.meodinger.lpfx.component.tools.*
import ink.meodinger.lpfx.options.*
import ink.meodinger.lpfx.util.HookedApplication
import ink.meodinger.lpfx.util.component.withOwner
import ink.meodinger.lpfx.util.property.onChange

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import java.awt.SystemTray
import java.awt.TrayIcon
import java.io.File
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
        private const val PARAM_UNNAMED_NO_CHECK_UPDATE = "--no-check-update"
    }

    private val state: State = State()
    private val icon: TrayIcon by lazy {
        TrayIcon(SwingFXUtils.fromFXImage(ICON, null)).apply {
            fun restore() {
                Platform.runLater {
                    state.stage.show()
                    state.stage.toFront()
                    SystemTray.getSystemTray().remove(this)
                }
            }
            fun destroy() {
                Platform.runLater {
                    if (!state.isOpened || !state.isChanged) this@LabelPlusFX.stop()
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

    /**
     * Try to minimal the window to system tray if SystemTray supported.
     * Otherwise, iconify it.
     */
    fun iconify() {
        if (Config.supportSysTray) {
            SystemTray.getSystemTray().add(icon)
            state.stage.hide()
        } else {
            state.stage.isIconified = true
        }
    }

    /**
     * Cheat Sheet. To display some hints on how to use LPFX
     */
    val cheatSheet: CheatSheet by lazy {
        CheatSheet() withOwner state.stage
    }

    /**
     * Online Dict. To search some text quick and simple
     */
    val onlineDict: OnlineDict by lazy {
        OnlineDict() withOwner state.stage
    }

    /**
     * Search & Replace. To search and replace some text in all TransFile
     */
    val searchAndReplace: SearchReplace by lazy {
        SearchReplace(state) withOwner state.stage
    }

    /**
     * Format Checker. Check format when save
     */
    val formatChecker: FormatChecker by lazy {
        FormatChecker(state) withOwner state.stage
    }

    /**
     * Specify Dialog. Specify files of pictures
     */
    val dialogSpecify: SpecifyFiles by lazy {
        SpecifyFiles(state) withOwner state.stage
    }

    /**
     * Log-related Dialog
     */
    val dialogLogs: AbstractPropertiesDialog by lazy {
        DialogLogs() withOwner state.stage
    }

    /**
     * Settings Dialog
     */
    val dialogSettings: AbstractPropertiesDialog by lazy {
        DialogSettings() withOwner state.stage
    }

    /**
     * Initialize the Options
     */
    override fun init() {
        // Let FX Thread keep running when Stage closed by BOSS Key
        Platform.setImplicitExit(false)

        state.application = this

        // Start Logger Timer
        Logger.tic()

        Logger.info("App initializing...", "Application")
        Options.load()
        Logger.info("App initialized", "Application")
    }

    /**
     * Start the Application.
     * The start order is `Stage` -> `View` -> `Controller` -> `Post-Start Operations`
     */
    override fun start(primaryStage: Stage) {
        Logger.info("App starting...", "Application")

        // FX Thread Catcher
        Thread.currentThread().setUncaughtExceptionHandler { _, e ->
            Logger.error("Exception uncaught in FX Thread", "Application")
            Logger.exception(e)
            if (state.isOpened) {
                showException(primaryStage, e, state.controller.emergency())
            } else {
                showException(primaryStage, e)
            }
        }

        state.stage = primaryStage

        // View & Controller
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

        // Start construct scene & stage
        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, Preference.windowWidth, Preference.windowHeight)
        primaryStage.setOnCloseRequest { if (!controller.stay()) stop() else it.consume() }

        // Window Size Listener
        val windowSizeListener: ChangeListener<Number> = onChange {
            if (primaryStage.isMaximized) return@onChange
            Preference.windowWidth = primaryStage.scene.width
            Preference.windowHeight = primaryStage.scene.height
        }
        primaryStage.scene.widthProperty().addListener(windowSizeListener)
        primaryStage.scene.heightProperty().addListener(windowSizeListener)

        // BOSS key
        var counter = 0
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) {
                counter++
                if (counter == 2) {
                    iconify()
                    counter = 0
                }
            }
        }

        // Show
        primaryStage.show()

        Logger.info("App started", "Application")

        // region Post-Start Operations

        // Check update
        if (PARAM_UNNAMED_NO_CHECK_UPDATE !in parameters.unnamed) if (Settings.autoCheckUpdate) controller.checkUpdate()
        // Open file
        if (parameters.raw.isNotEmpty() && File(parameters.raw.last()).isFile) {
            // Open the given file
            val file = File(parameters.raw.last())
            Platform.runLater { state.controller.open(file, file.parentFile) }
        } else if (Settings.autoOpenLastFile) {
            // Open last
            val file = RecentFiles.lastFile?.takeIf(File::exists)?.takeIf(File::isFile)
            if (file != null) Platform.runLater { state.controller.open(file, file.parentFile) }
        }
        // Register restore hook
        if (Config.isWin && Config.usingSWPrism) {
            state.application.addShutdownHook("RestorePrism", ::useHardwarePrism)
        }

        // endregion

        Logger.toc()
    }

    /**
     * Stop the Application.
     * The close order is `Hooks` -> `Options` -> `Stage`
     */
    override fun stop() {
        Logger.info("App stopping...", "Application")

        runHooks {
            Logger.error("Exception occurred during hooks run", "Application")
            Logger.exception(it)
        }
        Options.save()
        state.stage.close()

        Platform.exit()
    }

}

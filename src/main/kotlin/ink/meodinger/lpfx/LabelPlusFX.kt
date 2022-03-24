package ink.meodinger.lpfx

import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.options.Settings
import ink.meodinger.lpfx.util.HookedApplication
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.property.onChange
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.scene.Scene
import javafx.stage.Stage


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
            controller = Controller(root, state)
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

        val windowSizeListener: ChangeListener<Number> = onChange {
            if (primaryStage.isMaximized) return@onChange
            Preference.windowWidth = primaryStage.scene.width
            Preference.windowHeight = primaryStage.scene.height
        }
        primaryStage.scene.widthProperty().addListener(windowSizeListener)
        primaryStage.scene.heightProperty().addListener(windowSizeListener)

        primaryStage.show()

        Logger.info("App started", LOGSRC_APPLICATION)
        controller.labelInfo(I18N["common.ready"], LOGSRC_APPLICATION)

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

}

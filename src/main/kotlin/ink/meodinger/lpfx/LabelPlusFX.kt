package ink.meodinger.lpfx

import ink.meodinger.lpfx.io.UpdateChecker
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.util.HookedApplication
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get

import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.system.exitProcess


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
 */

/**
 * LPFX Application
 */
class LabelPlusFX: HookedApplication() {

    init {
        Logger.info("App initializing...", LOGSRC_APPLICATION)

        Options.load()

        // Cannot catch Exceptions occurred when starting
        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            Logger.exception(e)
            showException(e, State.stage)
        }

        State.application = this

        Logger.info("App initialized", LOGSRC_APPLICATION)
    }

    override fun start(primaryStage: Stage) {
        Logger.info("App starting...", LOGSRC_APPLICATION)

        State.stage = primaryStage

        val root: View
        val controller: Controller
        try {
            root = View()
            controller = Controller(root)
        } catch (e: Throwable) {
            Logger.exception(e)
            showException(e, null)
            stop()
            return
        }

        State.controller = controller

        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT)
        primaryStage.setOnCloseRequest {
            if (!controller.stay()) stop() else it.consume()
        }

        primaryStage.show()
        controller.labelInfo(I18N["common.ready"])

        Logger.info("App started", LOGSRC_APPLICATION)

        UpdateChecker.check()
    }

    override fun stop() {
        Logger.info("App stopping...", LOGSRC_APPLICATION)

        State.stage.close()
        Options.save()

        Logger.info("App stopped", LOGSRC_APPLICATION)
        runHooks { exitProcess(0) }
    }
}

package ink.meodinger.lpfx

import ink.meodinger.lpfx.io.UpdateChecker
import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.options.Preference
import ink.meodinger.lpfx.util.HookedApplication
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get

import javafx.application.Platform
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

    init {
        Logger.info("App initializing...", LOGSRC_APPLICATION)

        Options.load()

        // Cannot catch Exceptions occurred when starting
        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            Logger.exception(e)
            showException(State.stage, e)
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
            showException(null, e)
            stop()
            return
        }

        State.controller = controller

        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, Preference.windowWidth, Preference.windowHeight)
        primaryStage.setOnCloseRequest { if (!controller.stay()) exit() else it.consume() }

        Preference.windowWidthProperty().bind(primaryStage.scene.widthProperty())
        Preference.windowHeightProperty().bind(primaryStage.scene.heightProperty())

        primaryStage.show()

        Logger.info("App started", LOGSRC_APPLICATION)
        controller.labelInfo(I18N["common.ready"], LOGSRC_APPLICATION)

        UpdateChecker.check()
    }

    override fun exit() {
        Logger.info("App stopping...", LOGSRC_APPLICATION)

        State.stage.close()
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

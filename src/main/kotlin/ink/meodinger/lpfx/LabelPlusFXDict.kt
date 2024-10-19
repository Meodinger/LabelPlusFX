package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.dialog.showException
import ink.meodinger.lpfx.component.tools.OnlineDict
import ink.meodinger.lpfx.options.Logger

import javafx.application.Application
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/5/25
 * Have fun with my code!
 */

/**
 * Only Open Dictionary. This Application will be started
 * when using command-line argument `--dictionary`.
 */
class LabelPlusFXDict: Application() {

    /**
     * Start the Standalone Dict
     */
    override fun start(primaryStage: Stage) {
        // FX Thread Catcher
        Thread.currentThread().setUncaughtExceptionHandler { _, e ->
            Logger.error("Exception uncaught in FX Thread", "Application")
            Logger.exception(e)
            showException(primaryStage, e)
        }
        // Set up the Stage
        primaryStage.icons.add(ICON)
        primaryStage.title = "LPFX Dictionary (Standalone)"
        primaryStage.scene = OnlineDict().scene
        primaryStage.width = 400.0
        primaryStage.height = 400.0
        primaryStage.show()
    }

}

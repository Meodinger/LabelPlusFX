package ink.meodinger.lpfx

import ink.meodinger.lpfx.component.tools.OnlineDict

import javafx.application.Application
import javafx.stage.Stage

/**
 * Author: Meodinger
 * Date: 2022/5/25
 * Have fun with my code!
 */

/**
 * Only Open Dict
 */
class LabelPlusFXDict: Application() {

    /**
     * Start Standalone Dict
     */
    override fun start(primaryStage: Stage) {
        val dict = OnlineDict()

        primaryStage.title = "${dict.title} (Standalone)"
        primaryStage.scene = dict.scene
        primaryStage.width = 300.0
        primaryStage.height = 400.0
        primaryStage.show()
    }

}

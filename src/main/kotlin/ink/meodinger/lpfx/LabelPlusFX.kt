package ink.meodinger.lpfx

import ink.meodinger.lpfx.options.Logger
import ink.meodinger.lpfx.options.Options
import ink.meodinger.lpfx.util.Promise
import ink.meodinger.lpfx.util.dialog.initDialogOwner
import ink.meodinger.lpfx.util.dialog.showException
import ink.meodinger.lpfx.util.resource.ICON
import ink.meodinger.lpfx.util.resource.INFO
import ink.meodinger.lpfx.util.resource.get

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: ink.meodinger.lpfx
 */

/**
 * LPFX Application
 */
class LabelPlusFX: Application() {

    private val shutdownHooks = ArrayList<(() -> Unit) -> Unit>()

    /**
     * Add a shutdown hook for LabelPlusFX
     * Should use the resolve function as callback
     */
    fun addShutdownHook(onShutdown: (() -> Unit) -> Unit) = shutdownHooks.add(onShutdown)

    init {
        Options.load()

        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
            Logger.exception(e)
            showException(e)
        }

        State.application = this
    }

    override fun start(primaryStage: Stage) {
        State.stage = primaryStage

        val loader = FXMLLoader(javaClass.getResource("Window.fxml"))
        val root = loader.load<Parent>()
        val controller = loader.getController<Controller>()

        primaryStage.title = INFO["application.name"]
        primaryStage.icons.add(ICON)
        primaryStage.scene = Scene(root, WIDTH, HEIGHT)
        primaryStage.setOnCloseRequest {
            controller.exit()
            it.consume()
        }

        State.controller = controller
        initDialogOwner(primaryStage)

        primaryStage.show()

        Logger.info("App start", "Application")
    }

    override fun stop() {
        State.stage.close()

        Logger.info("App stop", "Application")

        Options.save()

        if (shutdownHooks.isEmpty()) Platform.exit()

        Promise.all(List(shutdownHooks.size) { Promise<Unit> { resolve, _ ->
            shutdownHooks[it] { resolve(Unit) }
        } }) catch { e: Exception ->
            Logger.exception(e)
            e
        } finally {
            Platform.exit()
        }
    }
}
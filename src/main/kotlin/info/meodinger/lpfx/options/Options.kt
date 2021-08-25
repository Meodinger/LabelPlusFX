package info.meodinger.lpfx.options

import info.meodinger.lpfx.util.dialog.*
import info.meodinger.lpfx.util.printExceptionToErrorLog
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Options {

    private const val LPFX = ".lpfx"
    private const val FileName_Config = "config"
    private const val FileName_Settings = "settings"
    private const val FileName_RecentFiles = "recent_files"
    private const val FolderName_ErrorLog = "error_log"

    val lpfx: Path = Paths.get(System.getProperty("user.home")).resolve(LPFX)
    val config: Path = lpfx.resolve(FileName_Config)
    val settings: Path = lpfx.resolve(FileName_Settings)
    val recentFiles: Path = lpfx.resolve(FileName_RecentFiles)
    val errorLog: Path = lpfx.resolve(FolderName_ErrorLog)

    fun init() {
        try {
            // project data folder
            if (Files.notExists(lpfx)) Files.createDirectories(lpfx)
            if (Files.notExists(errorLog)) Files.createDirectories(errorLog)

            // config
            initConfig()
            // settings
            initSettings()
            // recent_files
            initRecentFiles()
        } catch (e: IOException) {
            showException(e)
            showError(I18N["error.initialize_options_failed"])
            exitProcess(0)
        }
    }

    @Throws(IOException::class)
    private fun initConfig() {
        if (Files.notExists(config)) {
            Files.createFile(config)
            Config.save()
        }
        try {
            Config.load()
            Config.check()
        } catch (e: Exception) {
            Config.useDefault()
            printExceptionToErrorLog(e)
            showDialog(null, ALERT, I18N["common.alert"], null, "alert.load_option_failed.format")
        }
        Runtime.getRuntime().addShutdownHook(Thread { Config.save() })
    }

    @Throws(IOException::class)
    private fun initSettings() {
        if (Files.notExists(settings)) {
            Files.createFile(settings)
            Settings.save()
        }
        try {
            Settings.load()
            Settings.check()
        } catch (e: Exception) {
            Settings.useDefault()
            printExceptionToErrorLog(e)
            showDialog(null, ALERT, I18N["common.alert"], null, "alert.load_option_failed.format")
        }
        Runtime.getRuntime().addShutdownHook(Thread { Settings.save() })
    }

    @Throws(IOException::class)
    private fun initRecentFiles() {
        if (Files.notExists(recentFiles)) {
            Files.createFile(recentFiles)
            RecentFiles.save()
        }
        try {
            RecentFiles.load()
            RecentFiles.check()
        } catch (e: Exception) {
            RecentFiles.useDefault()
            printExceptionToErrorLog(e)
            showDialog(null, ALERT, I18N["common.alert"], null, "alert.load_option_failed.format")
        }
        Runtime.getRuntime().addShutdownHook(Thread { RecentFiles.save() })
    }

}
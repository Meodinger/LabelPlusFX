package info.meodinger.lpfx.options

import info.meodinger.lpfx.util.dialog.*
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
    private const val FileName_Preference = "preference"
    private const val FileName_Settings = "settings"
    private const val FileName_RecentFiles = "recent_files"
    private const val FolderName_logs = "logs"

    val lpfx: Path = Paths.get(System.getProperty("user.home")).resolve(LPFX)
    val preference: Path = lpfx.resolve(FileName_Preference)
    val settings: Path = lpfx.resolve(FileName_Settings)
    val recentFiles: Path = lpfx.resolve(FileName_RecentFiles)
    val logs: Path = lpfx.resolve(FolderName_logs)

    fun init() {
        try {
            // project data folder
            if (Files.notExists(lpfx)) Files.createDirectories(lpfx)
            if (Files.notExists(logs)) Files.createDirectories(logs)

            // config
            initConfig()
            // settings
            initSettings()
            // recent_files
            initRecentFiles()
        } catch (e: IOException) {
            Logger.fatal("Options init failed", "Options")
            Logger.exception(e)
            showException(e)
            showError(I18N["error.initialize_options_failed"])
            exitProcess(0)
        }
    }

    @Throws(IOException::class)
    private fun initConfig() {
        if (Files.notExists(preference)) {
            Files.createFile(preference)
            Preference.save()
        }
        try {
            Preference.load()
            Preference.check()

            Logger.info("Preference loaded", "Options")
            Logger.debug("Preference got:", Preference.properties, "Options")
        } catch (e: Exception) {
            Preference.useDefault()
            Preference.save()
            Logger.warning("Preference load failed, using default", "Options")
            Logger.exception(e)
            showDialog(
                null,
                2,
                I18N["common.alert"],
                null,
                if (e is CPropertyException)
                    String.format(I18N["alert.option.broken.format.s"], FileName_Preference)
                else
                    String.format(I18N["alert.option.load_failed.format.s"], FileName_Preference)
            )
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            Preference.save()
            Logger.info("Preference saved", "Options")
        })
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

            Logger.info("Settings loaded", "Options")
            Logger.debug("Settings got:", Settings.properties, "Options")
        } catch (e: Exception) {
            Settings.useDefault()
            Settings.save()
            Logger.warning("Settings load failed, using default", "Options")
            Logger.exception(e)
            showDialog(
                null,
                2,
                I18N["common.alert"],
                null,
                if (e is CPropertyException)
                    String.format(I18N["alert.option.broken.format.s"], FileName_Settings)
                else
                    String.format(I18N["alert.option.load_failed.format.s"], FileName_Settings)
            )
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            Settings.save()
            Logger.info("Settings saved", "Options")
        })
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

            Logger.info("RecentFiles loaded", "Options")
            Logger.debug("RecentFiles got:", RecentFiles.properties, "Options")
        } catch (e: Exception) {
            RecentFiles.useDefault()
            RecentFiles.save()
            Logger.warning("Recent Files loaded failed", "Options")
            Logger.exception(e)
            showDialog(
                null,
                2,
                I18N["common.alert"],
                null,
                if (e is CPropertyException)
                    String.format(I18N["alert.option.broken.format.s"], FileName_RecentFiles)
                else
                    String.format(I18N["alert.option.load_failed.format.s"], FileName_RecentFiles)
            )
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            RecentFiles.save()
            Logger.info("RecentFiles saved", "Options")
        })
    }

}
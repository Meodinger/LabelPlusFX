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

/**
 * The manager for all options
 */
object Options {

    private const val LPFX = ".lpfx"
    private const val FileName_Preference = "preference"
    private const val FileName_Settings = "settings"
    private const val FileName_RecentFiles = "recent_files"
    private const val FolderName_Logs = "logs"

    private lateinit var profileDir: Path
    val preference: Path get() = profileDir.resolve(FileName_Preference)
    val settings: Path get() = profileDir.resolve(FileName_Settings)
    val recentFiles: Path get() = profileDir.resolve(FileName_RecentFiles)
    val logs: Path get() = profileDir.resolve(FolderName_Logs)

    fun init(dirname: String = LPFX) {
        profileDir = Paths.get(System.getProperty("user.home")).resolve(dirname)

        // project data folder
        if (Files.notExists(profileDir)) Files.createDirectories(profileDir)
        if (Files.notExists(logs)) Files.createDirectories(logs)
    }

    fun load() {
        try {
            // recent_files
            loadRecentFiles()
            // config
            loadPreference()
            // settings
            loadSettings()

            Logger.level = Logger.LogType.valueOf(Settings[Settings.LogLevelPreference].asString())

            Logger.debug("RecentFiles got:", RecentFiles.getAll(), "Options")
            Logger.debug("Preference got:", Preference.properties, "Options")
            Logger.debug("Settings got:", Settings.properties, "Options")
        } catch (e: IOException) {
            Logger.fatal("Options load failed", "Options")
            Logger.exception(e)
            showException(e)
            showError(I18N["error.initialize_options_failed"])
            exitProcess(0)
        }
    }

    fun save() {
        RecentFiles.save()
        Logger.info("RecentFiles saved", "Options")

        Preference.save()
        Logger.info("Preference saved", "Options")

        Settings.save()
        Logger.info("Settings saved", "Options")
    }

    @Throws(IOException::class)
    private fun loadRecentFiles() {
        if (Files.notExists(recentFiles)) {
            Files.createFile(recentFiles)
            RecentFiles.save()
        }
        try {
            RecentFiles.load()
            RecentFiles.check()

            Logger.info("RecentFiles loaded", "Options")
        } catch (e: Exception) {
            RecentFiles.useDefault()
            RecentFiles.save()
            Logger.warning("Recent Files load failed", "Options")
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
    }

    @Throws(IOException::class)
    private fun loadPreference() {
        if (Files.notExists(preference)) {
            Files.createFile(preference)
            Preference.save()
        }
        try {
            Preference.load()
            Preference.check()

            Logger.info("Preference loaded", "Options")
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
    }

    @Throws(IOException::class)
    private fun loadSettings() {
        if (Files.notExists(settings)) {
            Files.createFile(settings)
            Settings.save()
        }
        try {
            Settings.load()
            Settings.check()

            Logger.info("Settings loaded", "Options")
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
    }

}
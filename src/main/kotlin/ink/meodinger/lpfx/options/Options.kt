package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.LOGSRC_OPTIONS
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.options.CProperty.CPropertyException

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import kotlin.system.exitProcess


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Have fun with my code!
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

            Logger.debug("Got RecentFiles:", RecentFiles.getAll(), LOGSRC_OPTIONS)
            Logger.debug("Got Preference:", Preference.properties, LOGSRC_OPTIONS)
            Logger.debug("Got Settings:", Settings.properties, LOGSRC_OPTIONS)
        } catch (e: IOException) {
            Logger.fatal("Load Options failed", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(I18N["error.options.load_failed"], null)
            showException(e, null)
            exitProcess(0)
        }
    }

    fun save() {
        RecentFiles.save()
        Logger.info("Saved RecentFiles", LOGSRC_OPTIONS)

        Preference.save()
        Logger.info("Saved Preference", LOGSRC_OPTIONS)

        Settings.save()
        Logger.info("Saved Settings", LOGSRC_OPTIONS)
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

            Logger.info("Loaded RecentFile", LOGSRC_OPTIONS)
        } catch (e: Exception) {
            RecentFiles.useDefault()
            RecentFiles.save()
            Logger.error("Load Recent Files failed", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(
                I18N["common.alert"],
                null,
                if (e is CPropertyException) {
                    String.format(I18N["error.options.broken.s"], FileName_RecentFiles)
                } else {
                    String.format(I18N["error.options.load_failed.s"], FileName_RecentFiles)
                },
                null
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

            Logger.info("Loaded Preferences", LOGSRC_OPTIONS)
        } catch (e: Exception) {
            Preference.useDefault()
            Preference.save()
            Logger.error("Load Preference failed, using default", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(
                I18N["common.alert"],
                null,
                if (e is CPropertyException) {
                    String.format(I18N["error.options.broken.s"], FileName_Preference)
                } else {
                    String.format(I18N["error.options.load_failed.s"], FileName_Preference)
                },
                null
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

            Logger.info("Loaded Settings", LOGSRC_OPTIONS)
        } catch (e: Exception) {
            Settings.useDefault()
            Settings.save()
            Logger.error("Load Settings failed, using default", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(
                I18N["common.alert"],
                null,
                if (e is CPropertyException) {
                    String.format(I18N["error.options.broken.s"], FileName_Settings)
                } else {
                    String.format(I18N["error.options.load_failed.s"], FileName_Settings)
                },
                null
            )
        }
    }

}

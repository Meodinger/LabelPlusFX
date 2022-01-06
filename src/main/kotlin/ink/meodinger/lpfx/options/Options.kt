package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.LOGSRC_OPTIONS
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.resource.I18N
import ink.meodinger.lpfx.util.resource.get
import ink.meodinger.lpfx.util.string.isMathematicalNatural

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.io.path.name
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
    private const val Logfile_MAXCOUNT = 20
    private const val Logfile_ALIVE = 3 * 24 * 60 * 60 * 1000L // 3 Days

    lateinit var profileDir: Path
        private set

    val preference:  Path get() = profileDir.resolve(FileName_Preference)
    val settings:    Path get() = profileDir.resolve(FileName_Settings)
    val recentFiles: Path get() = profileDir.resolve(FileName_RecentFiles)
    val logs:        Path get() = profileDir.resolve(FolderName_Logs)

    fun init(dirname: String = LPFX) {
        if (this::profileDir.isInitialized) throw IllegalStateException("Already set profile dirname")

        profileDir = Paths.get(System.getProperty("user.home")).resolve(dirname)

        // project data folder
        if (Files.notExists(profileDir)) Files.createDirectories(profileDir)
        if (Files.notExists(logs)) Files.createDirectories(logs)
    }

    fun load() {
        try {
            loadRecentFiles()
            loadPreference()
            loadSettings()
            cleanLogs()

            Logger.level = Logger.LogType.valueOf(Settings[Settings.LogLevelPreference].asString())

            Logger.debug("Got RecentFiles: ${AbstractProperties.getProperties(RecentFiles)}", LOGSRC_OPTIONS)
            Logger.debug("Got Preference:  ${AbstractProperties.getProperties(Preference)}", LOGSRC_OPTIONS)
            Logger.debug("Got Settings: ${AbstractProperties.getProperties(Settings)}", LOGSRC_OPTIONS)
        } catch (e: IOException) {
            Logger.fatal("Load Options failed", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(I18N["error.options.load_failed"], null)
            showException(e, null)
            exitProcess(-1)
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
            if (RecentFiles.checkAndFix()) {
                Logger.warning("Fixed $FileName_RecentFiles", LOGSRC_OPTIONS)
                showWarning(String.format(I18N["warning.options.fixed.s"], FileName_RecentFiles), null)
            }

            Logger.info("Loaded RecentFile", LOGSRC_OPTIONS)
        } catch (e: IOException) {
            RecentFiles.useDefault()
            RecentFiles.save()
            Logger.error("Load Recent Files failed", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(
                I18N["common.alert"],
                null,
                String.format(I18N["error.options.load_failed.s"], FileName_RecentFiles),
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
            if (Preference.checkAndFix()) {
                Logger.warning("Fixed $FileName_Preference", LOGSRC_OPTIONS)
                showWarning(String.format(I18N["warning.options.fixed.s"], FileName_Preference), null)
            }

            Logger.info("Loaded Preferences", LOGSRC_OPTIONS)
        } catch (e: IOException) {
            Preference.useDefault()
            Preference.save()
            Logger.error("Load Preference failed, using default", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(
                I18N["common.alert"],
                null,
                String.format(I18N["error.options.load_failed.s"], FileName_Preference),
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
            if (Settings.checkAndFix()) {
                Logger.warning("Fixed $FileName_Settings", LOGSRC_OPTIONS)
                showWarning(String.format(I18N["warning.options.fixed.s"], FileName_Settings), null)
            }

            Logger.info("Loaded Settings", LOGSRC_OPTIONS)
        } catch (e: IOException) {
            Settings.useDefault()
            Settings.save()
            Logger.error("Load Settings failed, using default", LOGSRC_OPTIONS)
            Logger.exception(e)
            showError(
                I18N["common.alert"],
                null,
                String.format(I18N["error.options.load_failed.s"], FileName_Settings),
                null
            )
        }
    }

    @Throws(IOException::class)
    private fun cleanLogs() {
        val failed = ArrayList<File>()

        try {
            Files.walk(logs, 1)
                .filter { it.name != logs.name }
                .map { it.toFile() }
                .collect(Collectors.toList())
                .apply { sortByDescending { it.lastModified() } }
                .forEachIndexed { index, file ->
                    val del = index > Logfile_MAXCOUNT || !file.name.isMathematicalNatural() || Date().time - file.lastModified() > Logfile_ALIVE
                    if (del && !file.delete()) failed.add(file)
                }
        } catch (e : IOException) {
            Logger.warning("Error occurred when checking old logs, clean procedure cancelled", LOGSRC_OPTIONS)
            Logger.exception(e)
            return
        }

        if (failed.isNotEmpty()) {
            // Try one more time
            failed.forEach { it.deleteOnExit() }

            val names = failed.joinToString("\n") { it.name }
            Logger.warning("Some error occurred when cleaning following old logs: \n$names", LOGSRC_OPTIONS)
        } else {
            Logger.info("Old logs cleaned", LOGSRC_OPTIONS)
        }
    }

}

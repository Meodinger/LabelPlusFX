package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.LOG_SRC_OTHER
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.once
import ink.meodinger.lpfx.util.dialog.*
import ink.meodinger.lpfx.util.string.isMathematicalNatural

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
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

    private const val FileName_Preference = "preference"
    private const val FileName_Settings = "settings"
    private const val FileName_RecentFiles = "recent_files"
    private const val FolderName_Logs = "logs"
    private const val Logfile_MAXCOUNT = 20

    private var profileDir: Path by once()

    val preference:  Path get() = profileDir.resolve(FileName_Preference)
    val settings:    Path get() = profileDir.resolve(FileName_Settings)
    val recentFiles: Path get() = profileDir.resolve(FileName_RecentFiles)
    val logs:        Path get() = profileDir.resolve(FolderName_Logs)

    fun init(dirname: String = ".lpfx") {
        profileDir = Paths.get(System.getProperty("user.home")).resolve(dirname)

        // project data folder
        if (Files.notExists(profileDir)) Files.createDirectories(profileDir)
        if (Files.notExists(logs)) Files.createDirectories(logs)
    }

    fun load() {
        try {
            loadProperties(RecentFiles, recentFiles)
            loadProperties(Preference, preference)
            loadProperties(Settings, settings)
            cleanLogs()

            Logger.level = Settings.logLevel

            Logger.debug("Got RecentFiles:\n$RecentFiles", LOG_SRC_OTHER)
            Logger.debug("Got Preference:\n$Preference", LOG_SRC_OTHER)
            Logger.debug("Got Settings:\n$Settings", LOG_SRC_OTHER)
        } catch (e: IOException) {
            Logger.fatal("Load Options failed", LOG_SRC_OTHER)
            Logger.exception(e)
            showError(null, I18N["error.options.load_failed"])
            showException(null, e)
            exitProcess(-1)
        }
    }

    fun save() {
        saveProperties(RecentFiles)
        saveProperties(Preference)
        saveProperties(Settings)
    }

    @Throws(IOException::class)
    private fun loadProperties(instance: AbstractProperties, path: Path) {
        if (Files.notExists(path)) Files.createFile(path)

        try {
            instance.load()
            Logger.info("Loaded ${instance.name}", LOG_SRC_OTHER)
        } catch (e: NumberFormatException) {
            instance.useDefault()
            AbstractProperties.save(path, instance)
            instance.load()

            Logger.error("Load ${instance.name} properties failed", LOG_SRC_OTHER)
            Logger.exception(e)
            showError(
                null,
                null,
                String.format(I18N["error.options.load_failed.s"], instance.name),
                I18N["common.alert"]
            )
        }
    }

    private fun saveProperties(instance: AbstractProperties) {
        instance.save()
        Logger.info("Saved ${instance.name}", LOG_SRC_OTHER)
    }

    @Throws(IOException::class)
    private fun cleanLogs() {
        val failed = ArrayList<File>()

        try {
            var count = 0
            Files
                .walk(logs, 1).filter { it.name != logs.name }
                .map(Path::toFile).collect(Collectors.toList())
                .apply { sortByDescending(File::lastModified) }
                .forEach { file ->
                    val del = count++ > Logfile_MAXCOUNT || !file.name.isMathematicalNatural()
                    if (del && !file.delete()) failed.add(file)
                }
        } catch (e : IOException) {
            Logger.warning("Error occurred when checking old logs, clean procedure cancelled", LOG_SRC_OTHER)
            Logger.exception(e)
            return
        }

        if (failed.isNotEmpty()) {
            // Try one more time
            failed.forEach(File::deleteOnExit)

            val names = failed.joinToString("\n") { it.name }
            Logger.warning("Some error occurred when cleaning following old logs: \n$names", LOG_SRC_OTHER)
        } else {
            Logger.info("Old logs cleaned", LOG_SRC_OTHER)
        }
    }

}

package ink.meodinger.lpfx.options

import ink.meodinger.lpfx.I18N
import ink.meodinger.lpfx.V
import ink.meodinger.lpfx.component.dialog.showError
import ink.meodinger.lpfx.component.dialog.showException
import ink.meodinger.lpfx.get
import ink.meodinger.lpfx.util.Version
import ink.meodinger.lpfx.util.doNothing
import ink.meodinger.lpfx.util.file.transfer
import ink.meodinger.lpfx.util.once
import ink.meodinger.lpfx.util.string.isMathematicalNatural

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.io.path.name
import kotlin.io.path.createTempFile
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

    // region Paths

    internal val preference:  Path by lazy { profileDir.resolve(FileName_Preference) }
    internal val settings:    Path by lazy { profileDir.resolve(FileName_Settings) }
    internal val recentFiles: Path by lazy { profileDir.resolve(FileName_RecentFiles) }
    internal val logs:        Path by lazy { profileDir.resolve(FolderName_Logs) }

    // endregion

    fun init(dirname: String = ".lpfx") {
        profileDir = Paths.get(System.getProperty("user.home")).resolve(dirname).resolve(V.toString())

        // project data folder
        if (Files.notExists(profileDir)) Files.createDirectories(profileDir)
        if (Files.notExists(logs)) Files.createDirectories(logs)
    }

    fun load() {
        try {
            loadProperties(RecentFiles)
            loadProperties(Preference)
            loadProperties(Settings)
            cleanLogs()

            Logger.level = Settings.logLevel

            Logger.debug("Got RecentFiles:\n$RecentFiles", "Options")
            Logger.debug("Got Preference:\n$Preference", "Options")
            Logger.debug("Got Settings:\n$Settings", "Options")
        } catch (e: IOException) {
            Logger.fatal("Load Options failed", "Options")
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
    private fun loadProperties(instance: AbstractProperties) {
        // Unknown properties will be defaults, so we just need to
        // make sure the file we will load exists.
        if (Files.notExists(instance.path)) {
            val file = Files.createFile(instance.path).toFile()
            findOldProperties(instance.path)?.let { transfer(it,file) }
            Logger.info("Loaded ${instance.name} in old version to use for the new version ", "Options")
        }

        try {
            instance.load()
            Logger.info("Loaded ${instance.name}", "Options")
        } catch (e: Throwable) {
            // Copy invalid properties file to temp, prepare for sending
            val tempFile = createTempFile().toFile()
            Logger.info("tempFile: ${tempFile.path}", "Options")
            try {
                transfer(instance.path.toFile(), tempFile)
            } catch (e: Throwable) {
                doNothing()
                Logger.error("create ${instance.name} properties failed, using default", "Options")
            } finally {
                tempFile.deleteOnExit()
            }

            // Export a valid file and reload. Fatal IOException may occur here
            instance.useDefault()
            AbstractProperties.save(instance)
            instance.load()

            Logger.error("Load ${instance.name} properties failed, using default", "Options")
            Logger.exception(e)
            showError(null, String.format(I18N["error.options.load_failed.s"], instance.name))
            showException(null, e, tempFile)
        }
    }
    // find the old Properties to use for the new version
    private  fun findOldProperties(nowPropsPath :Path) :File?{
        val parentDir = nowPropsPath.parent.parent
        if(Files.list(parentDir).count() <=1) return null
        val oldPropsPath = Files.list(parentDir)
            .toList()
            .filter { Files.isDirectory(it) and (it.fileName.toString() != V.toString())  }
            .maxWithOrNull(compareBy { Version(it.fileName.toString())})

    return oldPropsPath?.resolve(nowPropsPath.fileName)?.toFile()
    }

    private fun saveProperties(instance: AbstractProperties) {
        try {
            instance.save()
            Logger.info("Saved ${instance.name}", "Options")
        } catch (e: Throwable) {
            Logger.error("Save ${instance.name} properties failed", "Options")
            Logger.exception(e)
        }
    }

    private fun cleanLogs() {
        val failed = ArrayList<File>()

        try {
            var count = 0
            Files
                .walk(logs, 1).filter { it.name != logs.name }
                .map(Path::toFile).collect(Collectors.toList())
                .apply { sortByDescending(File::lastModified) }
                .forEach { file ->
                    val del = count++ > Logfile_MAXCOUNT || !file.nameWithoutExtension.isMathematicalNatural()
                    if (del && !file.delete()) failed.add(file)
                }
        } catch (e : IOException) {
            Logger.warning("Error occurred when checking old logs, clean procedure cancelled", "Options")
            Logger.exception(e)
            return
        }

        if (failed.isNotEmpty()) {
            val names = failed.joinToString("\n", transform = File::getName)
            Logger.warning("Some error occurred when cleaning following old logs: \n$names", "Options")

            // Try one more time
            failed.forEach(File::deleteOnExit)
        } else {
            Logger.info("Old logs cleaned", "Options")
        }
    }

}

package info.meodinger.lpfx.options

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.io
 */
object RecentFiles : AbstractProperties() {

    private const val MAX_SIZE = 10
    private const val RECENT = "recent"

    val recent = ArrayList<String>()

    override fun load() {
        load(Options.recentFiles, this)

        recent.clear()
        recent.addAll(this[RECENT].asList())
    }

    override fun save() {
        this[RECENT].set(recent)

        save(Options.recentFiles, this)
    }

    fun getLastOpenFile(): String {
        return recent[0]
    }

    fun add(path: String) {
        recent.remove(path)
        recent.add(0, path)
        if (recent.size > MAX_SIZE) {
            recent.removeAt(MAX_SIZE)
        }
    }

    fun remove(path: String) {
        recent.remove(path)
    }
}
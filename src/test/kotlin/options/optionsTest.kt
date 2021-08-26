package options

import info.meodinger.lpfx.options.Config
import info.meodinger.lpfx.options.RecentFiles
import info.meodinger.lpfx.options.Settings

/**
 * Author: Meodinger
 * Date: 2021/8/1
 * Location: options
 */
fun optionsTest() {
    println("""
        |----- Options Test -----
        |Config:
        |${Config.MAIN_DIVIDER}: ${Config[Config.MAIN_DIVIDER]}
        |${Config.RIGHT_DIVIDER}: ${Config[Config.RIGHT_DIVIDER]}
        |
        |Settings:
        |${Settings.DefaultGroupColorList}: ${Settings[Settings.DefaultGroupColorList].asStringList()}
        |${Settings.DefaultGroupNameList}: ${Settings[Settings.DefaultGroupNameList].asStringList()}
        |
        |RecentFiles:
        |${RecentFiles.getAll().size}: ${RecentFiles.getAll().joinToString("\n")}
    """.trimMargin())
}
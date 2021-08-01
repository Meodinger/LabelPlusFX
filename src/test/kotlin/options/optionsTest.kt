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
        |----------
        |Config:
        |${Config.MAIN_DIVIDER}: ${Config[Config.MAIN_DIVIDER]}
        |${Config.RIGHT_DIVIDER}: ${Config[Config.RIGHT_DIVIDER]}
        |
        |Settings:
        |${Settings.DefaultColorList}: ${Settings[Settings.DefaultColorList].asList()}
        |${Settings.DefaultGroupList}: ${Settings[Settings.DefaultGroupList].asList()}
        |
        |RecentFiles:
        |${RecentFiles.recent.size}: ${RecentFiles.recent.joinToString("\n")}
        |----------
    """.trimIndent())
}
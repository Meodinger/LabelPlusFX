package options

import info.meodinger.lpfx.options.Preference
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
        |${Preference.MAIN_DIVIDER}: ${Preference[Preference.MAIN_DIVIDER]}
        |${Preference.RIGHT_DIVIDER}: ${Preference[Preference.RIGHT_DIVIDER]}
        |
        |Settings:
        |${Settings.DefaultGroupColorList}: ${Settings[Settings.DefaultGroupColorList].asStringList()}
        |${Settings.DefaultGroupNameList}: ${Settings[Settings.DefaultGroupNameList].asStringList()}
        |
        |RecentFiles:
        |${RecentFiles.getAll().size}: ${RecentFiles.getAll().joinToString("\n")}
    """.trimMargin())
}
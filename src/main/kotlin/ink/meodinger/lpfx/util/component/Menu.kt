package ink.meodinger.lpfx.util.component

import ink.meodinger.lpfx.util.doNothing

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem


/**
 * Author: Meodinger
 * Date: 2021/8/18
 * Location: info.meodinger.lpfx.util
 */

/**
 * Disable mnemonic parsing for all Menu & MenuItem
 */
fun MenuBar.disableMnemonicParsingForAll() {
    for (menu in this.menus) menu.disableMnemonicParsing()
}

/**
 * Disable mnemonic parsing all Menu & MenuItem
 */
fun Menu.disableMnemonicParsing() {
    isMnemonicParsing = false
    for (item in this.items) when (item) {
        is Menu, is MenuItem -> item.disableMnemonicParsing()
        else -> doNothing()
    }
}

/**
 * Disable mnemonic parsing for MenuItem
 */
fun MenuItem.disableMnemonicParsing() {
    isMnemonicParsing = false
}


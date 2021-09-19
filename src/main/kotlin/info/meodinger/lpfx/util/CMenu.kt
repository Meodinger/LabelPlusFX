package info.meodinger.lpfx.util

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
    this.isMnemonicParsing = false
    for (item in this.items) {
        if (item is MenuItem) item.disableMnemonicParsing()
        if (item is Menu) item.disableMnemonicParsing()
    }
}

/**
 * Disable mnemonic parsing for MenuItem
 */
fun MenuItem.disableMnemonicParsing() {
    this.isMnemonicParsing = false
}


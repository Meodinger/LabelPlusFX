package ink.meodinger.lpfx.util.ime

/**
 * Author: Meodinger
 * Date: 2022/3/21
 * Have fun with my code!
 */

/**
 * Language Tags
 */

// Simplified Chinese
const val ZH_HANS = "zh-Hans"
const val ZH_CN   = "zh-CN"

// Traditional Chinese
const val ZH_HANT = "zh-Hant"
const val ZH_HK   = "zh-HK"
const val ZH_TW   = "zh-TW"

// English
const val EN      = "en"
const val EN_US   = "en-US"
const val EN_GB   = "en-GB"

// Japanese
const val JA      = "ja"
const val JA_JP   = "ja-JP"

// Conversion Mode

enum class ImeConversionMode(val value: Int) {
    NATIVE                 (0x0000_0001),
    KATAKANA               (0x0000_0002),
    FULL_SHAPE             (0x0000_0004),
    ROMAN                  (0x0000_0008),
    CHAR_CODE              (0x0000_0010),
    NO_CONVERSION          (0x0000_0020),
    END_USER_DEFINED_CHAR  (0x0000_0040),
    SYMBOL                 (0x0000_0080),
    FIXED                  (0x0000_0100),
    ALPHA_NUMERIC          (0x0000_0200),
    DO_NOT_CARE            (-2147483648),
}

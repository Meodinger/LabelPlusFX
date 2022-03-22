package ink.meodinger.lpfx.util.ime

/**
 * Author: Meodinger
 * Date: 2022/3/21
 * Have fun with my code!
 */

/**
 * Language Tags
 */

@JvmInline
value class LangTag(val name: String)

// Simplified Chinese
val ZH_HANS = LangTag("zh-Hans")
val ZH_CN = LangTag("zh-CN")

// Traditional Chinese
val ZH_HANT = LangTag("zh-Hant")
val ZH_HK = LangTag("zh-HK")
val ZH_TW = LangTag("zh-TW")

// English
val EN = LangTag("en")
val EN_US = LangTag("en-US")
val EN_GB = LangTag("en-GB")

// Japanese
val JA = LangTag("ja")
val JA_JP = LangTag("ja-JP")


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

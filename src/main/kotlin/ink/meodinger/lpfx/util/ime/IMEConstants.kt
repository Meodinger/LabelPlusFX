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

// Conversion Status

enum class ImeConversionMode(val value: Int) {
    // ime_cmodes.h
    ALPHA_NUMERIC          (0b0000_0000),
    NATIVE                 (0b0000_0001),
    CHINESE                (0b0000_0001),
    HANGUL                 (0b0000_0001),
    JAPANESE               (0b0000_0001),
    KATAKANA               (0b0000_0010),
    LANGUAGE               (0b0000_0011),
    FULL_SHAPE             (0b0000_1000),
    ROMAN                  (0b0001_0000),
    CHAR_CODE              (0b0010_0000),
    HANJA_CONVERT          (0b0100_0000),
    NATIVE_SYMBOL          (0b1000_0000),
    // imm.h
    HANGEUL                (0b0000_0000_0001),
    SOFTKBD                (0b0000_1000_0000),
    NO_CONVERSION          (0b0001_0000_0000),
    END_USER_DEFINED_CHAR  (0b0010_0000_0000),
    SYMBOL                 (0b0100_0000_0000),
    FIXED                  (0b1000_0000_0000),
    RESERVED               (0xF000_0000.toInt());
}

enum class ImeSentenceMode(val value: Int) {
    NONE                   (0b0000_0000),
    PLURAL_CLAUSE          (0b0000_0001),
    SINGLE_CONVERT         (0b0000_0010),
    AUTOMATIC              (0b0000_0100),
    PHRASE_PREDICT         (0b0000_1000),
    CONVERSATION           (0b0001_0000),
    RESERVED               (0xF000)
}

enum class ImeMode { OFF, HIRAGANA, KATAKANA, ALPHA, KATAKANA_HALF, ALPHA_HALF, }

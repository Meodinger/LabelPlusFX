package info.meodinger.lpfx.options

/**
 * Author: Meodinger
 * Date: 2021/8/26
 * Location: info.meodinger.lpfx.options
 */
class CPropertyException(message: String) : RuntimeException(message) {

    companion object {
        fun propertyNotFound(key: String) = CPropertyException("Property `$key` not found")
        fun propertyValueInvalid(key: String, value: String) = CPropertyException("Value `$value` invalid for property `$key`")
        fun propertyElementInvalid(key: String, element: String) = CPropertyException("Element `$element` invalid for list property `$key`")
        fun propertyListSizeInvalid(key: String, size: Int) = CPropertyException("Size `${size}` invalid for list property `$key`")
    }

}
package info.meodinger.lpfx.options

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get

/**
 * Author: Meodinger
 * Date: 2021/8/26
 * Location: info.meodinger.lpfx.options
 */
class CPropertyException(message: String) : RuntimeException(message) {

    companion object {
        fun propertyNotFound(key: String) =
            CPropertyException(String.format(I18N["exception.property.property_not_found.format.k"], key))
        fun propertyValueInvalid(key: String, value: String) =
            CPropertyException(String.format(I18N["exception.property.property_value_invalid.format.vk"], value, key))
        fun propertyElementInvalid(key: String, element: String) =
            CPropertyException(String.format(I18N["exception.property.property_element_invalid.format.ek"], element, key))
        fun propertyListSizeInvalid(key: String, size: Int) =
            CPropertyException(String.format(I18N["exception.property.property_list_size_invalid.format.sk"], size, key))
    }

}
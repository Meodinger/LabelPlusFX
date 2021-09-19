package info.meodinger.lpfx.type

import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get


/**
 * Author: Meodinger
 * Date: 2021/8/26
 * Location: info.meodinger.lpfx.type
 */

/**
 * An exception class for TransFile
 */
class TransFileException(message: String) : RuntimeException(message) {

    companion object {
        fun transGroupNotFound(groupName: String) =
            TransFileException(String.format(I18N["exception.trans_file.trans_group_not_found.format.s"], groupName))
        fun groupIdInvalid(groupId: Int) =
            TransFileException(String.format(I18N["exception.trans_file.group_id_invalid.format.i"], groupId))
        fun pictureNotFound(picName: String) =
            TransFileException(String.format(I18N["exception.trans_file.picture_not_found.format.s"], picName))
        fun labelIndexInvalid(picName: String, labelIndex: Int) =
            TransFileException(String.format(I18N["exception.trans_file.label_index_invalid.is"], picName, labelIndex))
    }

}
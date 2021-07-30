package info.meodinger.lpfx.type

/**
 * Author: Meodinger
 * Date: 2021/7/30
 * Location: info.meodinger.lpfx.type
 */
data class TransGroup(
    var name: String = "NewGroup@${index++}",
    var color: String = "66CCFF"
) {
    companion object {
        private var index = 0
    }
}
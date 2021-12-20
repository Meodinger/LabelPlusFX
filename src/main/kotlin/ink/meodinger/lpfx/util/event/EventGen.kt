package ink.meodinger.lpfx.util.event

import javafx.event.EventTarget
import javafx.event.EventType
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * Author: Meodinger
 * Date: 2021/12/20
 * Have fun with my code!
 */


/**
 * Gen KeyEvent
 */
fun keyEvent(
    it:            KeyEvent,
    source:        Any                 = it.source,
    target:        EventTarget         = it.target,
    type:          EventType<KeyEvent> = it.eventType,
    character:     String              = it.character,
    text:          String              = it.text,
    code:          KeyCode             = it.code,
    isShiftDown:   Boolean             = it.isShiftDown,
    isControlDown: Boolean             = it.isControlDown,
    isAltDown:     Boolean             = it.isAltDown,
    isMetaDown:    Boolean             = it.isMetaDown
): KeyEvent {
    return KeyEvent(source, target, type, character, text, code, isShiftDown, isControlDown, isAltDown, isMetaDown)
}

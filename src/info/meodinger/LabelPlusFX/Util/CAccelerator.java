package info.meodinger.LabelPlusFX.Util;

import javafx.scene.input.GestureEvent;
import javafx.scene.input.KeyEvent;

/**
 * Author: Meodinger
 * Date: 2021/6/15
 * Location: info.meodinger.LabelPlusFX.Util
 */
public class CAccelerator {

    /**
     * Based on Windows Platform
     */

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static final boolean isMac = OS.contains("mac");

    public static boolean isControlDown(GestureEvent event) {
        if (event.isControlDown()) return true;
        if (isMac) return event.isMetaDown();
        return false;
    }

    public static boolean isControlDown(KeyEvent event) {
        if (event.isControlDown()) return true;
        if (isMac) return event.isMetaDown();
        return false;
    }

}

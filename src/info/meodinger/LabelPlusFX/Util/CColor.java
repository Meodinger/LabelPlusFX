package info.meodinger.LabelPlusFX.Util;

import javafx.scene.paint.Color;

/**
 * @author Meodinger
 * Date: 2021/5/28
 * Location: info.meodinger.LabelPlusFX.Component
 */
public class CColor {

    public static String toHex(Color color) {
        if (color == null) return "";
        return color.toString().substring(2, 8).toUpperCase();
    }

}

package info.meodinger.LabelPlusFX;

import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * Author: Meodinger
 * Date: 2021/6/2
 * Location: info.meodinger.LabelPlusFX.Util
 */
public class Resources {

    static {
        ICON = new Image(Resources.class.getResourceAsStream(getResource("icon.png")));
    }

    public static Image ICON;
    public static InputStream PS_Script_Stream;
    public static InputStream PS_Template_Stream_CN;
    public static InputStream PS_Template_Stream_EN;

    private static String getResource(String name) {
        return "Resources/" + name;
    }
    public static void reload() {
        PS_Script_Stream = Resources.class.getResourceAsStream(getResource("Meo_PS_Script"));
        PS_Template_Stream_CN = Resources.class.getResourceAsStream(getResource("zh.psd"));
        PS_Template_Stream_EN = Resources.class.getResourceAsStream(getResource("en.psd"));
    }

}

package info.meodinger.LabelPlusFX;

import java.io.InputStream;

/**
 * Author: Meodinger
 * Date: 2021/6/2
 * Location: info.meodinger.LabelPlusFX.Util
 */
public class Resources {

    public static InputStream ICON;
    public static InputStream PS_Script_Stream;
    public static InputStream PS_Template_Stream_CN;
    public static InputStream PS_Template_Stream_EN;

    private static String getResource(String name) {
        return "Resources/" + name;
    }
    public static void init() {
        ICON = Resources.class.getResourceAsStream(getResource("icon.png"));
        PS_Script_Stream = Resources.class.getResourceAsStream(getResource("Meo_PS_Script.jsx"));
        PS_Template_Stream_CN = Resources.class.getResourceAsStream(getResource("zh.psd"));
        PS_Template_Stream_EN = Resources.class.getResourceAsStream(getResource("en.psd"));
    }

}

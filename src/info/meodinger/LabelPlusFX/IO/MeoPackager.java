package info.meodinger.LabelPlusFX.IO;

import info.meodinger.LabelPlusFX.State;
import info.meodinger.LabelPlusFX.Resources;
import info.meodinger.LabelPlusFX.Type.TransFile;
import info.meodinger.LabelPlusFX.Util.CZip;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Author: Meodinger
 * Date: 2021/5/31
 * Location: info.meodinger.LabelPlusFX.IO
 */
public class MeoPackager {

    private final State state;

    public MeoPackager(State state) {
        this.state = state;
    }

    public boolean packMeo(String path) {
        Resources.reload();

        InputStream script = Resources.PS_Script_Stream;
        InputStream template_CN = Resources.PS_Template_Stream_CN;
        InputStream template_EN = Resources.PS_Template_Stream_EN;

        try {
            CZip zip = new CZip(path);

            zip.zip(script, "/Meo_PS_Script.jsx");
            zip.zip(template_CN, "/ps_script_res/zh.psd");
            zip.zip(template_EN, "/ps_script_res/en.psd");

            String content = TransFile.MeoTransFile.toJsonString(state.getTransFile());
            InputStream contentStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            zip.zip(contentStream, "/images/" + "translation.json");

            for (String picName : state.getSortedPicList()) {
                File pic = new File(state.getPicPathOf(picName));
                zip.zip(pic, "/images/" + picName);
            }

            zip.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


}

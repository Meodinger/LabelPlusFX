package info.meodinger.LabelPlusFX.IO;

import info.meodinger.LabelPlusFX.Config;
import info.meodinger.LabelPlusFX.Resources;
import info.meodinger.LabelPlusFX.Type.TransFile;
import info.meodinger.LabelPlusFX.Type.TransLabel;
import info.meodinger.LabelPlusFX.Util.CZip;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: Meodinger
 * Date: 2021/5/31
 * Location: info.meodinger.LabelPlusFX.IO
 */
public class MeoPackager {

    private final Config config;

    public MeoPackager(Config config) {
        this.config = config;
    }

    public boolean packMeo(String path) {
        Resources.init();

        InputStream script = Resources.PS_Script_Stream;
        InputStream template_CN = Resources.PS_Template_Stream_CN;
        InputStream template_EN = Resources.PS_Template_Stream_EN;

        CZip zip = new CZip(path);
        try {
            zip.zip(script, "/Meo_PS_Script.jsx");
            zip.zip(template_CN, "/ps_script_res/zh.psd");
            zip.zip(template_EN, "/ps_script_res/en.psd");

            TransFile.MeoTransFile transFile = config.getTransFile().clone();
            Map<String, List<TransLabel>> sort = new TreeMap<>(Comparator.naturalOrder());
            sort.putAll(transFile.getTransMap());
            transFile.setTransMap(sort);
            String content = JSON.toJSONString(transFile,
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.PrettyFormat
            );
            InputStream contentStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            zip.zip(contentStream, "/images/" + "translation.json");

            for (String picName : config.getSortedPicSet()) {
                File pic = new File(config.getPicPathOf(picName));
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

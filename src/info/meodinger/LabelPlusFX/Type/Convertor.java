package info.meodinger.LabelPlusFX.Type;

import info.meodinger.LabelPlusFX.I18N;
import info.meodinger.LabelPlusFX.Type.TransFile.LPTransFile;
import info.meodinger.LabelPlusFX.Type.TransFile.MeoTransFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Meodinger
 * Date: 2021/5/24
 * Location: info.meodinger.LabelPlusFX.Type
 */
public class Convertor {

    public static MeoTransFile lp2meo(LPTransFile translation) {
        List<String> groupListLP = translation.getGroup();

        assert groupListLP.size() <= 9;
        ArrayList<MeoTransFile.Group> gList = new ArrayList<>();
        for (int i = 0; i < groupListLP.size(); i++) {
            gList.add(new MeoTransFile.Group(groupListLP.get(i), MeoTransFile.DEFAULT_COLOR_LIST[i]));
        }

        MeoTransFile meo = new MeoTransFile();
        meo.setVersion(translation.getVersion());
        meo.setComment(translation.getComment());
        meo.setGroup(gList);
        meo.setTransMap(translation.getTransMap());

        return meo;
    }

    public static LPTransFile meo2lp(MeoTransFile translation) throws ConvException {

        if (translation.getGroup().size() > 9) throw new ConvException(translation.getGroup().size());

        List<MeoTransFile.Group> groupListMeo = translation.getGroup();
        ArrayList<String> gList = new ArrayList<>();
        for (MeoTransFile.Group group : groupListMeo) {
            gList.add(group.name);
        }

        LPTransFile lp = new LPTransFile();
        lp.setVersion(translation.getVersion());
        lp.setComment(translation.getComment());
        lp.setGroup(gList);
        lp.setTransMap(translation.getTransMap());

        return lp;
    }

    public static class ConvException extends Exception {

        public ConvException(String msg) {
            super(msg);
        }

        public ConvException(int groupCount) {
            super(String.format(I18N.FORMAT_TOO_MANY_GROUPS, groupCount));
        }
    }
}

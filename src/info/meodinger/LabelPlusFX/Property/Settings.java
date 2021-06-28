package info.meodinger.LabelPlusFX.Property;

import info.meodinger.LabelPlusFX.Property.Property.Key;
import info.meodinger.LabelPlusFX.Options;

import java.util.*;

/**
 * Author: Meodinger
 * Date: 2021/6/27
 * Location: info.meodinger.LabelPlusFX
 */
public final class Settings extends AbstractProperties {

    public static final Key DefaultColorList = new Key("DefaultColorList");
    public static final Key DefaultGroupList = new Key("DefaultGroupList");

    private Settings() {
        this.properties = new ArrayList<>(Arrays.asList(
                new Property("DefaultColorList", "FF0000",
                        "0000FF", "008000", "1E90FF", "FFD700",
                        "FF00FF", "A0522D", "FF4500", "9400D3"
                ),
                new Property("DefaultGroupList", "框外", "框内")
        ));
    }
    public static final Settings Instance = new Settings();

    @Override
    public void load() {
        AbstractProperties.load(Options.settings, this);
    }

    @Override
    public void save() {
        AbstractProperties.save(Options.settings, this);
    }
}

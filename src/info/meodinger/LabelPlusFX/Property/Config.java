package info.meodinger.LabelPlusFX.Property;

import info.meodinger.LabelPlusFX.Property.Property.Key;
import info.meodinger.LabelPlusFX.Options;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Author: Meodinger
 * Date: 2021/6/28
 * Location: info.meodinger.LabelPlusFX.Property
 */
public final class Config extends AbstractProperties {

    public static final Key MainDivider = new Key("MainDivider");
    public static final Key RightDivider = new Key("RightDivider");

    private Config() {
        this.properties = new ArrayList<>(Arrays.asList(
                new Property("MainDivider", 0.63),
                new Property("RightDivider", 0.6)
        ));
    }
    public static final Config Instance = new Config();


    @Override
    public void load() {
        AbstractProperties.load(Options.config, this);
    }

    @Override
    public void save() {
        AbstractProperties.save(Options.config, this);
    }
}

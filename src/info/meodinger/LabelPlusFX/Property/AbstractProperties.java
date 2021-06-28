package info.meodinger.LabelPlusFX.Property;

import info.meodinger.LabelPlusFX.Util.CDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Meodinger
 * Date: 2021/6/28
 * Location: info.meodinger.LabelPlusFX.Property
 */
public abstract class AbstractProperties {

    protected List<Property> properties;

    public abstract void load();
    public abstract void save();
    public Property get(Property.Key key) {
        for (Property property : properties) {
            if (property.key.equals(key.key)) {
                return property;
            }
        }
        throw new IllegalStateException("Property not found");
    }

    protected static void load(Path path, AbstractProperties instance) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<Property> all = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.trim().startsWith(Property.COMMENT_HEAD)) continue;

                String[] props = line.split(Property.KEY_VALUE_SEPARATOR, 2);
                all.add(new Property(props[0], props[1]));
            }

            instance.properties.clear();
            instance.properties.addAll(all);
        } catch (Exception e) {
            CDialog.showException(e);
        }
    }
    protected static void save(Path path, AbstractProperties instance) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Property property : instance.properties) {
                writer.write(property.key + Property.KEY_VALUE_SEPARATOR + property.asString());
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            CDialog.showException(e);
        }
    }
}

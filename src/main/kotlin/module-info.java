
module lpfx {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;
    requires java.desktop;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.fxml;
    requires jakarta.mail;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    // This three modules provide ogg SPI
    requires soundlibs.vorbisspi;
    requires soundlibs.jorbis;
    requires soundlibs.tritonus.share;

    opens info.meodinger.lpfx to javafx.fxml;
    opens info.meodinger.lpfx.component to javafx.fxml;
    opens info.meodinger.lpfx.component.common to javafx.fxml;
    opens info.meodinger.lpfx.type to com.fasterxml.jackson.databind;

    exports info.meodinger.lpfx;
    exports info.meodinger.lpfx.io;
    exports info.meodinger.lpfx.type;
    exports info.meodinger.lpfx.util;
    exports info.meodinger.lpfx.options;
    exports info.meodinger.lpfx.component;
    exports info.meodinger.lpfx.component.common;
    exports info.meodinger.lpfx.component.singleton;
}
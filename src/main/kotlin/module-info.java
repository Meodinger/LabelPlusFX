
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

    opens ink.meodinger.lpfx to javafx.fxml;
    opens ink.meodinger.lpfx.component to javafx.fxml;
    opens ink.meodinger.lpfx.component.common to javafx.fxml;
    opens ink.meodinger.lpfx.type to com.fasterxml.jackson.databind;

    exports ink.meodinger.lpfx;
    exports ink.meodinger.lpfx.io;
    exports ink.meodinger.lpfx.type;
    exports ink.meodinger.lpfx.util;
    exports ink.meodinger.lpfx.options;
    exports ink.meodinger.lpfx.component;
    exports ink.meodinger.lpfx.component.common;
    exports ink.meodinger.lpfx.component.singleton;
}
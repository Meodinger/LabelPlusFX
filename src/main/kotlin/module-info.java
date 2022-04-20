
module lpfx {
    // Kotlin
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;
    // Swing
    requires java.desktop;
    // JavaFX
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    // Mail
    requires jakarta.mail;
    // HTML Parser
    requires ink.meodinger.htmlparser;
    // JSON
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    // WebP Support.
    requires com.twelvemonkeys.imageio.webp;

    // Use services to enable webp support. Actually we don't need to
    // write this line because 12Monkeys are multi-release modules
    // that have specified service providers in META-INF/services.
    uses javax.imageio.spi.ImageReaderSpi;

    opens ink.meodinger.lpfx.type to com.fasterxml.jackson.databind;

    exports ink.meodinger.lpfx;
    exports ink.meodinger.lpfx.io;
    exports ink.meodinger.lpfx.type;
    exports ink.meodinger.lpfx.util;
    exports ink.meodinger.lpfx.options;
    exports ink.meodinger.lpfx.component;
    exports ink.meodinger.lpfx.component.common;
    exports ink.meodinger.lpfx.component.properties;
}

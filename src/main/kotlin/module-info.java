/**
 * Author: Meodinger
 * Date: 2021/8/24
 * Location: 
 */
module lpfx {
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk7;
    requires kotlin.stdlib.jdk8;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires tornadofx;
    requires jakarta.mail;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    opens info.meodinger.lpfx to javafx.fxml;
    opens info.meodinger.lpfx.component to javafx.fxml;
    opens info.meodinger.lpfx.type to com.fasterxml.jackson.databind;

    exports info.meodinger.lpfx;
}
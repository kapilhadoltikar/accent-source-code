module com.accent.theme2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.accent.theme2 to javafx.fxml;
    exports com.accent.theme2;
}
module com.example.servertemplateforcardsupdate2122 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;


    opens com.example.servertemplateforcardsupdate2122 to javafx.fxml;
    exports com.example.servertemplateforcardsupdate2122;
}
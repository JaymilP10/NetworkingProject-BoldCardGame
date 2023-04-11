module com.example.clienttemplateforcardsupdate2122 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;


    opens com.example.clienttemplateforcardsupdate2122 to javafx.fxml;
    exports com.example.clienttemplateforcardsupdate2122;
}
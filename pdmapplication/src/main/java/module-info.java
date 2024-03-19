module com.pdmapplication {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.pdmapplication to javafx.fxml;
    exports com.pdmapplication;
}

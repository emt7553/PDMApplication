module com.pdmapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.pdmapplication to javafx.fxml;
    exports com.pdmapplication;
}

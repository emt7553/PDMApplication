module com.pdmapplication {
    requires org.postgresql.jdbc;
    requires com.jcraft.jsch;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    opens com.pdmapplication to javafx.fxml;
    exports com.pdmapplication;
}

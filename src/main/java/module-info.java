module com.example.hotelreservationsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.hotelreservationsystem to javafx.fxml;
    exports com.example.hotelreservationsystem;
    exports com.example.hotelreservationsystem.controller;
    opens com.example.hotelreservationsystem.controller to javafx.fxml;
}
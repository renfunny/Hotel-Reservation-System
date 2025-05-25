package com.example.hotelreservationsystem.utils;

import com.example.hotelreservationsystem.controller.AdminController;
import com.example.hotelreservationsystem.model.Admin;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class SceneSwitcher {
    public static void switchToMainView(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/kiosk-view.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Hotel ABC");

    }

    public static void switchToBookingView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/booking-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setTitle("Booking");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void switchToGuestView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/guests-view.fxml"));
        Parent root = loader.load();


        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Guest Details");
    }

    public static void switchToRoomView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/rooms-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Rooms");

    }

    public static void switchToBookingDetailsView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/booking-details-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Booking Details");
    }

    public static void switchToAdminView(ActionEvent event, Admin admin) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/admin-view.fxml"));
        Parent root = loader.load();
        AdminController controller = loader.getController();
        controller.setLoggedInAdmin(admin);

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Admin");
    }

    public static void switchToFeedbackView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/hotelreservationsystem/feedback-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Feedback");
    }
}

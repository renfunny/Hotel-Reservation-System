package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.Admin;
import com.example.hotelreservationsystem.model.BookingSession;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import com.example.hotelreservationsystem.utils.ShowRules;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;

public class KioskController {
    @FXML private Button rulesBtn;

    @FXML
    public void initialize() {
        rulesBtn.setOnAction(e -> ShowRules.showRules());

        try(Connection conn = DatabaseUtil.getConnection()){
            Statement stmt = conn.createStatement();

            stmt.execute("CREATE TABLE IF NOT EXISTS RoomType(" +
                    "RoomTypeID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "TypeName VARCHAR(20) UNIQUE NOT NULL)");

            stmt.execute("INSERT INTO RoomType (TypeName) VALUES" +
                    "('SINGLE')," +
                    "('DOUBLE')," +
                    "('DELUXE')," +
                    "('PENTHOUSE')" +
                    "ON CONFLICT (TypeName) DO NOTHING");

            stmt.execute("CREATE TABLE IF NOT EXISTS Room(" +
                    "RoomID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "RoomTypeID INT REFERENCES RoomType(RoomTypeID)," +
                    "NumberOfBeds INT," +
                    "Price FLOAT NOT NULL," +
                    "Status VARCHAR(50))");

            stmt.execute("CREATE TABLE IF NOT EXISTS Guest(" +
                    "GuestID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Name VARCHAR(50) NOT NULL," +
                    "PhoneNumber VARCHAR(20)," +
                    "Email VARCHAR(50)," +
                    "Address TEXT," +
                    "FeedbackID INT," +
                    "FOREIGN KEY(FeedbackID) REFERENCES Feedbacks(FeedbackID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Reservation(" +
                    "ReservationID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "GuestID INT," +
                    "RoomID INT," +
                    "CheckInDate DATE," +
                    "CheckOutDate DATE," +
                    "NumberOfGuests INT," +
                    "Status VARCHAR(50)," +
                    "FOREIGN KEY(GuestID) REFERENCES Guest(GuestID) ON DELETE CASCADE," +
                    "FOREIGN KEY(RoomID) REFERENCES RoomType(RoomTypeID) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Billing(" +
                    "BillID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "ReservationID INT," +
                    "Amount FLOAT NOT NULL," +
                    "Tax FLOAT," +
                    "TotalAmount FLOAT," +
                    "Discount FLOAT," +
                    "FOREIGN KEY(ReservationID) REFERENCES Reservation(ReservationID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Feedbacks(" +
                    "FeedbackID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "GuestID INT," +
                    "ReservationID INT," +
                    "Comment TEXT," +
                    "Rating INT," +
                    "FOREIGN KEY(GuestID) REFERENCES Guest(GuestID) ON DELETE CASCADE," +
                    "FOREIGN KEY(ReservationID) REFERENCES Reservation(ReservationID) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Admin(" +
                    "AdminID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username VARCHAR(50) UNIQUE NOT NULL," +
                    "Password VARCHAR(50) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS Kiosk(" +
                    "KioskID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Location VARCHAR(50))");

            stmt.execute("INSERT INTO Room (RoomID, RoomTypeID, NumberOfBeds, Price, Status) VALUES " +
                    "(1, 1, 1, 50.0, 'AVAILABLE')," +
                    "(2, 1, 1, 50.0, 'AVALIABLE')," +
                    "(3, 1, 1, 50.0, 'AVAILABLE')," +
                    "(4, 1, 1, 50.0, 'AVALIABLE')," +
                    "(5, 1, 1, 50.0, 'AVAILABLE')," +
                    "(6, 1, 1, 50.0, 'AVALIABLE')," +
                    "(7, 2, 2, 60.0, 'AVAILABLE')," +
                    "(8, 2, 2, 60.0, 'AVAILABLE')," +
                    "(9, 2, 2, 60.0, 'AVAILABLE')," +
                    "(10, 2, 2, 60.0, 'AVAILABLE')," +
                    "(11, 2, 2, 60.0, 'AVAILABLE')," +
                    "(12, 2, 2, 60.0, 'AVAILABLE')," +
                    "(13, 3, 1, 80.0, 'AVAILABLE')," +
                    "(14, 3, 1, 80.0, 'AVAILABLE')," +
                    "(15, 3, 1, 80.0, 'AVAILABLE')," +
                    "(16, 3, 1, 80.0, 'AVAILABLE')," +
                    "(17, 3, 1, 80.0, 'AVAILABLE')," +
                    "(18, 3, 1, 80.0, 'AVAILABLE')," +
                    "(19, 4, 1, 100.0, 'AVAILABLE')," +
                    "(20, 4, 1, 100.0, 'MAINTENANCE')" +
                    "ON CONFLICT DO NOTHING");

            stmt.execute("INSERT INTO Admin (AdminID, Username, Password) VALUES " +
                    "(1, 'admin1', '12345')," +
                    "(2, 'admin2', '67891')" +
                    "ON CONFLICT DO NOTHING");



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBooking(ActionEvent event) throws IOException {
        SceneSwitcher.switchToBookingView(event);
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Admin Login");
        alert.setHeaderText("Admin/Staff Only");
        alert.setContentText("This area is restricted to hotel staff. Do you want to proceed?");

        ButtonType okButton = new ButtonType("OK");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            showAdminLoginForm(event);
        }
    }

    private void showAdminLoginForm(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Admin Login");

        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return username.getText() + ":" + password.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(credentials -> {
            String[] parts = credentials.split(":");
            String user = parts[0];
            String pass = parts[1];

            //  check credentials from the DB
            try (Connection conn = DatabaseUtil.getConnection()) {
                String sql = "SELECT * FROM Admin WHERE Username = ? AND Password = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, user);
                stmt.setString(2, pass);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION, "Login Successful");
                    success.showAndWait();
                    Admin admin = new Admin();
                    admin.setUsername(user);
                    admin.setPassword(pass);
                    SceneSwitcher.switchToAdminView(event, admin);
                } else {
                    Alert fail = new Alert(Alert.AlertType.ERROR, "Invalid credentials.");
                    fail.showAndWait();
                }

            } catch (SQLException | IOException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Database error.");
                error.showAndWait();
            }
        });
    }

    @FXML
    private void handleFeedback(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Feedback");
        alert.setHeaderText("Feedback is available only for checked-out guests.");
        alert.setContentText("Would you like to proceed?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Leave Feedback");
            dialog.setHeaderText("Only guests who have checked out can leave feedback.");
            dialog.setContentText("Please enter your Reservation ID:");

            Optional<String> result2 = dialog.showAndWait();

            result2.ifPresent(resIdStr -> {
                try {
                    int reservationId = Integer.parseInt(resIdStr);

                    if (isReservationCheckedOut(reservationId)) {
                        BookingSession.currentReservationID = reservationId;
                        SceneSwitcher.switchToFeedbackView(event);
                    } else {
                        showAlert("Not Allowed", "Only checked-out guests can leave feedback.");
                    }

                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter a valid numeric Reservation ID.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "An error occurred while loading the feedback view.");
                }
            });
        }
    }

    private boolean isReservationCheckedOut(int reservationId) {
        boolean checkedOut = false;
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT Status FROM Reservation WHERE ReservationID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reservationId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String status = rs.getString("Status");
                checkedOut = "CHECKED_OUT".equalsIgnoreCase(status);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checkedOut;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
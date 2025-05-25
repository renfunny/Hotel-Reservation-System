package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.BookingSession;
import com.example.hotelreservationsystem.model.Guest;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GuestsController {

    @FXML private TextField nameTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField addressTextField;

    @FXML
    private void initialize() {
        try{
            loadGuestIfExists();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void loadGuestIfExists() throws SQLException {
        String sql = " SELECT g.Name, g.PhoneNumber, g.Email, g.Address " +
        "FROM Guest g " +
        "JOIN Reservation r ON g.GuestID = r.GuestID " +
        "WHERE r.ReservationID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, BookingSession.currentReservationID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nameTextField.setText(rs.getString("Name"));
                    phoneTextField.setText(rs.getString("PhoneNumber"));
                    emailTextField.setText(rs.getString("Email"));
                    addressTextField.setText(rs.getString("Address"));
                }
            }
        }
    }

    private int createGuest(String name, String phone, String email, String address) throws SQLException {
        String insertSql = "INSERT INTO Guest (Name, PhoneNumber, Email, Address) VALUES (?, ?, ?, ?)";
        String getIdSql = "SELECT last_insert_rowid() as id";

        try(Connection conn = DatabaseUtil.getConnection();
            PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            insertStmt.setString(1, name);
            insertStmt.setString(2, phone);
            insertStmt.setString(3, email);
            insertStmt.setString(4, address);
            insertStmt.executeUpdate();

            // Get the last inserted ID using a separate query
            try (PreparedStatement getIdStmt = conn.prepareStatement(getIdSql);
                 ResultSet rs = getIdStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Failed to get the generated guest ID");
                }
            }
        }
    }


    private boolean reservationHasGuest() throws SQLException {
        String sql = "SELECT GuestID FROM Reservation WHERE ReservationID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, BookingSession.currentReservationID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject("GuestID") != null;  // true if guest already exists
                }
            }
        }
        return false;
    }

    private void updateReservationWithGuest(int guestID) throws SQLException {
        String sql = "UPDATE Reservation SET GuestID = ? WHERE ReservationID = ?";

        try(Connection conn = DatabaseUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

                stmt.setInt(1, guestID);
                stmt.setInt(2, BookingSession.currentReservationID);
                stmt.executeUpdate();
        }
    }

    @FXML
    private void handleNextBtn(ActionEvent event) {
        String name = nameTextField.getText().trim();
        String phone = phoneTextField.getText().trim();
        String email = emailTextField.getText().trim();
        String address = addressTextField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            showAlert("Error", "Please enter all the fields correctly.");
            return;
        }

        try{
            if(!reservationHasGuest()) {
                int guestID = createGuest(name, phone, email, address);
                updateReservationWithGuest(guestID);

                Guest guest = new Guest();
                Map<String, Object> details = new HashMap<>();
                details.put("GuestID", guestID);
                details.put("Name", name);
                details.put("PhoneNumber", phone);
                details.put("Email", email);
                details.put("Address", address);

                guest.setGuestDetails(details);

            }
            SceneSwitcher.switchToRoomView(event);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleBackBtn(ActionEvent event) throws IOException {
        SceneSwitcher.switchToBookingView(event);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

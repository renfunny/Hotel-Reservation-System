package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.BookingSession;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BookingDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label roomIDLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label guestNumberLabel;
    @FXML private Label checkInLabel;
    @FXML private Label checkOutLabel;
    @FXML private Label bedNumberLabel;

    @FXML
    private void initialize() {
        displayBookingDetails();
    }

    private void displayBookingDetails(){
        try(Connection conn = DatabaseUtil.getConnection()){
            String sql = "SELECT g.Name, res.RoomID, rt.TypeName, r.NumberOfBeds, r.Price, " +
                    "res.NumberOfGuests, res.CheckInDate, res.CheckOutDate " +
                    "FROM Reservation res " +
                    "JOIN Room r ON res.RoomID = r.RoomID " +
                    "JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "JOIN Guest g ON res.GuestID = g.GuestID " + // join with Guest table
                    "WHERE res.ReservationID = ?";

           PreparedStatement stmt = conn.prepareStatement(sql);
           stmt.setInt(1, BookingSession.currentReservationID); // set the reservation ID from BookingSession

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameLabel.setText("Guest Name: " + rs.getString("Name"));
                roomIDLabel.setText("Room Number: " + rs.getInt("RoomID"));
                roomTypeLabel.setText("Room Type: " + rs.getString("TypeName"));
                bedNumberLabel.setText("Number of Beds: " + rs.getInt("NumberOfBeds"));
                guestNumberLabel.setText("Number of Guests: " + rs.getInt("NumberOfGuests"));
                LocalDate checkIn = rs.getDate("CheckInDate").toLocalDate();
                LocalDate checkOut = rs.getDate("CheckOutDate").toLocalDate();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                checkInLabel.setText("Check-In Date: " + checkIn.format(formatter));
                checkOutLabel.setText("Check-Out Date: " + checkOut.format(formatter));
            } else {
                showAlert("Error", "No reservation found with the provided Booking ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load booking details.");
        }
    }

    @FXML
    private void handleBookBtn(ActionEvent event) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // update reservation status to "Confirmed"
            String updateReservationSql = "UPDATE Reservation SET Status = ? WHERE ReservationID = ?";
            try (PreparedStatement updateReservationStmt = conn.prepareStatement(updateReservationSql)) {
                updateReservationStmt.setString(1, "CONFIRMED");
                updateReservationStmt.setInt(2, BookingSession.currentReservationID);
                int rowsUpdated = updateReservationStmt.executeUpdate();

                if (rowsUpdated > 0) {
                    //update room status to "Occupied"
                    String updateRoomSql = "UPDATE Room SET Status = ? WHERE RoomID = ?";
                    try (PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoomSql)) {
                        updateRoomStmt.setString(1, "OCCUPIED");
                        updateRoomStmt.setInt(2, BookingSession.selectedRoomID);
                        updateRoomStmt.executeUpdate();
                    }
                    //show success alert
                    showConfirmation("Booking Confirmed", "Your booking has been confirmed successfully.");

                    BookingSession.currentReservationID = null;
                    BookingSession.selectedRoomID = null;
                    BookingSession.numberOfGuests = 0;
                    SceneSwitcher.switchToMainView(event);
                } else {
                    showAlert("Error", "Failed to confirm the booking. Reservation not found.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to finalize the booking.");
        }
    }

    @FXML
    private void handleBackBtn(ActionEvent event) throws IOException {
        SceneSwitcher.switchToRoomView(event);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

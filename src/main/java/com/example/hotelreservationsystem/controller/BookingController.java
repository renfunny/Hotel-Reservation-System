package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.BookingSession;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import com.example.hotelreservationsystem.utils.ShowRules;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class BookingController {

    @FXML private Button rulesBtn;
    @FXML private DatePicker checkinDatePicker;
    @FXML private DatePicker checkoutDatePicker;
    @FXML private ChoiceBox<Integer> guestNoChoiceBox;


    @FXML
    public void initialize() {
        guestNoChoiceBox.getItems().addAll(IntStream.rangeClosed(1, 4).boxed().toList());
        rulesBtn.setOnAction(e -> ShowRules.showRules());
        if(BookingSession.currentReservationID == null) {
            createPendingReservation();
        } else {
            populateBookingSession();
        }
    }

    private void createPendingReservation() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // First, get the next available ID
            String getMaxIdSql = "SELECT COALESCE(MAX(ReservationID), 0) + 1 AS NextId FROM Reservation";
            try (PreparedStatement getMaxStmt = conn.prepareStatement(getMaxIdSql)) {
                ResultSet rs = getMaxStmt.executeQuery();
                if (rs.next()) {
                    int nextId = rs.getInt("NextId");

                    // Insert the new reservation with the calculated ID
                    String insertSql = "INSERT INTO Reservation (ReservationID, Status) VALUES (?, 'PENDING')";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, nextId);
                        insertStmt.executeUpdate();
                        BookingSession.currentReservationID = nextId;
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, "Failed to create PENDING Reservation", e);
            e.printStackTrace();
        }
    }


    private void populateBookingSession() {
        if (BookingSession.currentReservationID == null) return;

        String sql = "SELECT CheckInDate, CheckOutDate, NumberOfGuests FROM Reservation WHERE ReservationID = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, BookingSession.currentReservationID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Date checkInDate = rs.getDate("CheckInDate");
                    Date checkOutDate = rs.getDate("CheckOutDate");
                    int guestNo = rs.getInt("NumberOfGuests");

                    if (checkInDate != null) {
                        checkinDatePicker.setValue(checkInDate.toLocalDate());
                    }

                    if (checkOutDate != null) {
                        checkoutDatePicker.setValue(checkOutDate.toLocalDate());
                    }

                    guestNoChoiceBox.setValue(guestNo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "DELETE FROM Reservation WHERE ReservationID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, BookingSession.currentReservationID);
            stmt.executeUpdate();

            BookingSession.currentReservationID = null;
            BookingSession.numberOfGuests = 0;
            SceneSwitcher.switchToMainView(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to cancel reservation.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleNext(ActionEvent event) {
        LocalDate checkIn = checkinDatePicker.getValue();
        LocalDate checkOut = checkoutDatePicker.getValue();
        Integer guests = guestNoChoiceBox.getValue();

        if (checkIn == null || checkOut == null || guests == null) {
            showAlert("Missing Fields", "Please complete all fields before continuing.");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            showAlert("Invalid Dates", "Checkout date must be after check-in date.");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "UPDATE Reservation SET CheckInDate = ?, CheckOutDate = ?, NumberOfGuests = ? WHERE ReservationID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(checkIn));
            stmt.setDate(2, Date.valueOf(checkOut));
            stmt.setInt(3, guests);
            stmt.setInt(4, BookingSession.currentReservationID);
            stmt.executeUpdate();

            BookingSession.numberOfGuests = guests;
            SceneSwitcher.switchToGuestView(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to update reservation.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}

package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.Reservation;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;

public class BillController {
    @FXML private Label guestNameLabel;
    @FXML private Label roomPriceLabel;
    @FXML private Label nightsLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    private Stage dialogStage;
    private Reservation reservation;
    private String guestName;
    private double roomPrice;
    private double tax;
    private double subtotal;
    private double discount;
    private double total;
    private boolean confirmed = false;



    public void setGuestName(String name) {
        this.guestName = name;
    }

    public void setRoomPrice(double price) {
        this.roomPrice = price;
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        populateBill();
    }

    public void populateBill() {
        guestNameLabel.setText("Guest: " + guestName);
        roomPriceLabel.setText(String.format("Price: $%.2f/night", roomPrice));

        long numNights = ChronoUnit.DAYS.between(
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );
        nightsLabel.setText("Stay: " + numNights + " nights");

        subtotal = roomPrice * numNights;
        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
        tax = subtotal * 0.13;
        taxLabel.setText(String.format("Tax: $%.2f", tax));
        discountLabel.setText("Discount : -$0.00");
        total = subtotal + tax;
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void handleApplyDiscount() {
        discount = total * 0.2;
        total = total - discount;
        discountLabel.setText(String.format("Discount: -$%.2f", discount));
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        dialogStage.close();
    }

    @FXML
    private void handleConfirm() {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "INSERT INTO Billing (ReservationID, Amount, Tax, TotalAmount, Discount) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, reservation.getReservationID());
            stmt.setDouble(2, subtotal);
            stmt.setDouble(3, tax);
            stmt.setDouble(4, total);
            stmt.setDouble(5, discount);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                showConfirm("Payment Successful", "Bill has been saved and guest is checked out.");

            } else {
                showAlert("Error", "Failed to save bill.");
            }

            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while saving the bill.");
        }
        confirmed = true;
        dialogStage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

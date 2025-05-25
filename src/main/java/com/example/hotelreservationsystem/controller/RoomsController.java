package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.BookingSession;
import com.example.hotelreservationsystem.model.RoomType;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import com.example.hotelreservationsystem.utils.ShowRules;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomsController {

    @FXML private Button rulesBtn;
    @FXML private ChoiceBox<RoomType> roomTypeChoiceBox;
    @FXML private Label maxOccupancyLabel;
    @FXML private Label priceLabel;
    @FXML
    private void initialize() {
        rulesBtn.setOnAction(e -> ShowRules.showRules());
        List<String> allowedRoomTypes = getAllowedRoomTypes(BookingSession.numberOfGuests);

        try (Connection conn = DatabaseUtil.getConnection()) {
            String placeholders = String.join(",", Collections.nCopies(allowedRoomTypes.size(), "?"));
            String sql = "SELECT DISTINCT rt.TypeName " +
                    "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE r.Status = 'AVAILABLE' AND rt.TypeName IN (" + placeholders + ")";
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int j = 0; j < allowedRoomTypes.size(); j++) {
                stmt.setString(j + 1, allowedRoomTypes.get(j));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomTypeChoiceBox.getItems().add(RoomType.valueOf(rs.getString("TypeName")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        roomTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case SINGLE:
                        maxOccupancyLabel.setText("2 People Max");
                        priceLabel.setText("Price: $50");
                        break;
                    case DOUBLE:
                        maxOccupancyLabel.setText("4 People Max");
                        priceLabel.setText("Price: $60");
                        break;
                    case DELUXE:
                        maxOccupancyLabel.setText("2 People Max");
                        priceLabel.setText("Price: $80");
                        break;
                    case PENTHOUSE:
                        maxOccupancyLabel.setText("2 People Max");
                        priceLabel.setText("Price: $100");
                        break;
                    default:
                        maxOccupancyLabel.setText("-");
                        priceLabel.setText("-");
                        break;
                }
            }
        });
    }

    private List<String> getAllowedRoomTypes(int guests) {
        if (guests <= 2) {
            return List.of("SINGLE", "DOUBLE", "DELUXE", "PENTHOUSE");
        } else if (guests <= 4) {
            return List.of("DOUBLE"); // limit to max 4 people
        } else {
            return new ArrayList<>();
        }
    }

    @FXML void handleNextBtn(ActionEvent event) {
        String selectedType = String.valueOf(roomTypeChoiceBox.getValue());

        if (selectedType == null || selectedType.isEmpty()) {
            showAlert("Error", "Please select a room type");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            String sql = "SELECT r.RoomID " +
                    "FROM Room r " +
                    "JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE rt.TypeName = ? AND r.Status = 'AVAILABLE' " +
                    "LIMIT 1"; // get one available room

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int roomID = rs.getInt("RoomID");

                String updateSql = "UPDATE Room SET Status = 'PENDING' WHERE RoomID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, roomID);
                updateStmt.executeUpdate();

                String updateReservationSql = "UPDATE Reservation SET RoomID = ? WHERE ReservationID = ?";
                PreparedStatement updateReservationStmt = conn.prepareStatement(updateReservationSql);
                updateReservationStmt.setInt(1, roomID);
                updateReservationStmt.setInt(2, BookingSession.currentReservationID);
                updateReservationStmt.executeUpdate();

                conn.commit(); // commit transaction
                // save BookingSession
                BookingSession.selectedRoomID = roomID;

                // go to booking-details-view
                SceneSwitcher.switchToBookingDetailsView(event);

            } else {
                showAlert("Valid Room Selection Required", "Room Selection not valid, please try again");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(e.getMessage(), "Room Selection not valid, please try again");
        }
    }

    @FXML
    private void handleBackBtn(ActionEvent event) {
        try {
            BookingSession.selectedRoomID = null;
            SceneSwitcher.switchToGuestView(event);
        } catch (IOException e) {
            e.printStackTrace();
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

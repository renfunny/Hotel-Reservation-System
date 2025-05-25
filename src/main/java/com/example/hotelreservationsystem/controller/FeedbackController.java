package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.BookingSession;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import com.example.hotelreservationsystem.utils.ShowRules;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedbackController {
    @FXML private Button rulesBtn;
    @FXML private Slider ratingSlider;
    @FXML private TextArea commentTextArea;

    @FXML
    private void initialize() {
        rulesBtn.setOnAction((ActionEvent event) -> ShowRules.showRules());
    }

    @FXML
    private void handleSubmitBtn(ActionEvent event) throws SQLException {
        int reservationID = BookingSession.currentReservationID;
        int rating = (int) ratingSlider.getValue();
        String comment = commentTextArea.getText();

        try(Connection conn = DatabaseUtil.getConnection()){
            int guestID = -1;
            String sql = "SELECT GuestID FROM Reservation WHERE ReservationID = ?";
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, reservationID);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()){
                    guestID = rs.getInt("GuestID");
                }
            }
            if(guestID == -1){
                showAlert("Error fetching guest", "Guest was not found for the selected reservation");
                return;
            }

            String insertSQL = "INSERT INTO Feedbacks (GuestID, ReservationID, Comment, Rating) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setInt(1, guestID);
                pstmt.setInt(2, reservationID);
                pstmt.setString(3, comment);
                pstmt.setInt(4, rating);
                pstmt.executeUpdate();
            }
            BookingSession.currentReservationID = null;
            SceneSwitcher.switchToMainView(event);
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

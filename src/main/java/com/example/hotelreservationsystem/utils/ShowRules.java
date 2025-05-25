package com.example.hotelreservationsystem.utils;

import javafx.scene.control.Alert;

public class ShowRules {
    public static void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hotel Rules");
        alert.setHeaderText("Rules and Regulations");
        alert.setContentText("""
            • Single room: Max two people.
            • Double room: Max 4 people.
            • Deluxe and Pent rooms: Max two people but the prices are higher.
            • More than 2 adults less than 5 can have Double room or two single rooms will be offered.
            • More than 4 adults will have multiple Double or combination of Double and single rooms.
        """);
        alert.showAndWait();
    }
}

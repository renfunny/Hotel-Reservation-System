package com.example.hotelreservationsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String DB_PATH = "jdbc:sqlite:C:\\Users\\renFunny\\IdeaProjects\\HotelReservationSystem\\src\\main\\java\\com\\example\\hotelreservationsystem\\database\\hotel.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_PATH);
    }
}

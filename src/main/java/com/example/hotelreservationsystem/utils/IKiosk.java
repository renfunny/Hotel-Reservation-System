package com.example.hotelreservationsystem.utils;

public interface IKiosk {
    void displayWelcomeMessage();
    void guideBookingProcess();
    boolean validateInput();
    boolean confirmBooking();
}

package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IKiosk;

public class Kiosk implements IKiosk {
    private int kioskID;
    private String location;

    @Override
    public void displayWelcomeMessage() {}

    @Override
    public void guideBookingProcess() {

    }

    @Override
    public boolean validateInput() {
        return false;
    }

    @Override
    public boolean confirmBooking() {
        return false;
    }
}

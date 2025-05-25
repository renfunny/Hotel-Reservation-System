package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IReservation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Reservation implements IReservation {
    private int reservationID;
    private int guestID;
    private int roomID;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private String status;

    public Reservation() {}

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public int getGuestID() {
        return guestID;
    }

    public void setGuestID(int guestID) {
        this.guestID = guestID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean createReservation() {
        this.status = "Confirmed";
        return true;
    }

    @Override
    public boolean cancelReservation() {
        this.status = "Canceled";
        return true;
    }

    @Override
    public Map<String, Object> getReservationDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("ReservationID", reservationID);
        details.put("GuestID", guestID);
        details.put("RoomID", roomID);
        details.put("CheckInDate", checkInDate);
        details.put("CheckOutDate", checkOutDate);
        details.put("NumberOfGuests", numberOfGuests);
        details.put("Status", status);
        return details;
    }

    @Override
    public boolean confirmReservation() {
        return "Confirmed".equalsIgnoreCase(status);
    }
}

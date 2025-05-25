package com.example.hotelreservationsystem.utils;

import java.util.Map;

public interface IReservation {
    boolean createReservation();
    boolean cancelReservation();
    Map<String, Object> getReservationDetails();
    boolean confirmReservation();
}

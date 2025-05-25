package com.example.hotelreservationsystem.utils;

import java.util.Dictionary;
import java.util.Map;

public interface IGuest {
    Map<String, Object> getGuestDetails();
    void setGuestDetails(Map<String, Object> details);
    boolean validateGuestDetails();
}

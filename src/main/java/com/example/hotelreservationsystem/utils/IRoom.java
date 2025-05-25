package com.example.hotelreservationsystem.utils;

import java.util.Map;

public interface IRoom {
    Map<String, Object> getRoomDetails();
    void setRoomDetails(Map<String, Object> details);
    boolean checkRoomAvailability();
}

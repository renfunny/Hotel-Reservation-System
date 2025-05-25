package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IRoom;

import java.util.HashMap;
import java.util.Map;

public class Room implements IRoom {
    private int roomID;
    private RoomType roomType;
    private int numberOfBeds;
    private float price;
    private String status;

    @Override
    public Map<String, Object> getRoomDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("RoomID", roomID);
        details.put("RoomType", roomType.toString());
        details.put("NumberOfBeds", numberOfBeds);
        details.put("Price", price);
        details.put("Status", status);
        return details;
    }

    @Override
    public void setRoomDetails(Map<String, Object> details) {
        this.roomID = (int) details.get("RoomID");
        this.roomType = (RoomType) details.get("RoomType");
        this.numberOfBeds = (int) details.get("NumberOfBeds");
        this.price = (float) details.get("Price");
        this.status = (String) details.get("Status");
    }

    @Override
    public boolean checkRoomAvailability(){
        return "Available".equalsIgnoreCase(status);
    }

}

package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IGuest;

import java.util.HashMap;
import java.util.Map;

public class Guest implements IGuest {
    private int guestID;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private int feedback;

    @Override
    public Map<String, Object> getGuestDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("GuestID", guestID);
        details.put("Name", name);
        details.put("PhoneNumber", phoneNumber);
        details.put("Email", email);
        details.put("Address", address);
        return details;
    }

    @Override
    public void setGuestDetails(Map<String, Object> details) {
        this.guestID = (int) details.get("GuestID");
        this.name = (String) details.get("Name");
        this.phoneNumber = (String) details.get("PhoneNumber");
        this.email = (String) details.get("Email");
        this.address = (String) details.get("Address");
    }

    @Override
    public boolean validateGuestDetails() {
        return name != null && !name.isEmpty() && email != null && !email.isEmpty();
    }

    public int getGuestID() {
        return guestID;
    }

    public void setGuestID(int guestID) {
        this.guestID = guestID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFeedback() {
        return feedback;
    }

    public void setFeedback(int feedback) {
        this.feedback = feedback;
    }
}

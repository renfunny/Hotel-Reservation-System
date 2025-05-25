package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IFeedback;

import java.util.HashMap;
import java.util.Map;

public class Feedback implements IFeedback {
    private int feedbackID;
    private int guestID;
    private int reservationID;
    private String comments;
    private float rating;

    @Override
    public boolean submitFeedback() {
        return rating >=1 && rating <=5;
    }

    @Override
    public Map<String, Object> getFeedbackDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("FeedbackID", feedbackID);
        details.put("GuestID", guestID);
        details.put("ReservationID", reservationID);
        details.put("Comments", comments);
        details.put("Rating", rating);
        return details;
    }
}

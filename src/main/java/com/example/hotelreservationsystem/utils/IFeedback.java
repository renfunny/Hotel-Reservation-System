package com.example.hotelreservationsystem.utils;

import java.util.Map;

public interface IFeedback {
    boolean submitFeedback();
    Map<String, Object> getFeedbackDetails();
}

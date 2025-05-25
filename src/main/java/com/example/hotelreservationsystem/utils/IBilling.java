package com.example.hotelreservationsystem.utils;

import java.util.Map;

public interface IBilling {
    Map<String, Object> generateBill();
    boolean applyDiscount();
    float calculateTotal();
    void printBill();
}

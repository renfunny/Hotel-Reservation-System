package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IBilling;

import java.util.HashMap;
import java.util.Map;

public class Billing implements IBilling {
    private int billID;
    private int reservationID;
    private float amount;
    private float tax;
    private float totalAmount;
    private float discount;

    @Override
    public Map<String, Object> generateBill() {
        totalAmount = amount + tax - discount;
        Map<String, Object> billDetails = new HashMap<>();
        billDetails.put("BillID", billID);
        billDetails.put("ReservationID", reservationID);
        billDetails.put("TotalAmount", totalAmount);
        return billDetails;
    }

    @Override
    public boolean applyDiscount() {
        discount = amount * 0.1f;
        return true;
    }

    @Override
    public float calculateTotal() {
        return amount + tax - discount;
    }

    @Override
    public void printBill() {
        System.out.println("BillID: " + billID);
        System.out.println("Reservation ID: " + reservationID);
        System.out.println("Amount: " + amount);
        System.out.println("Tax: " + tax);
        System.out.println("Total Amount: " + totalAmount);
        System.out.println("Discount: " + discount);
    }
}

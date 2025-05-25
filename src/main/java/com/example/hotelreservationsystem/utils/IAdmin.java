package com.example.hotelreservationsystem.utils;

import java.util.Map;

public interface IAdmin {
    boolean login();
    boolean searchGuest();
    boolean checkOutGuest();
    Map<String, Object> generateReport();

}

package com.example.hotelreservationsystem.model;

import com.example.hotelreservationsystem.utils.IAdmin;

import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Admin implements IAdmin {
    private static final Logger logger = Logger.getLogger(Admin.class.getName());

    private String username;
    private String password;

    static {
        try {
            FileHandler fileHandler = new FileHandler("System_logs.%g.log", 1024 * 1024, 10, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e){
            logger.log(Level.SEVERE, "Failed to set up file logging", e);
        }
    }

    public Admin(){
    }

    public Admin(String username, String password){
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean login() {
        boolean success = "admin".equals(username) && "12345".equals(password);
        if(success){
            logger.info("Admin logged in successful for user: " + username);
        } else {
            logger.warning("Failed admin login attempt for user: " + username);
        }
        return success;
    }

    @Override
    public boolean searchGuest() {
        logger.info("Admin searched for a guest");
        return true;
    }

    @Override
    public boolean checkOutGuest() {
        logger.info("Admin checked out a guest");
        return true;
    }

    @Override
    public Map<String, Object> generateReport() {
        logger.info("Admin generated a report.");
        // Mocked values
        return Map.of(
                "TotalGuests", 120,
                "CheckedIn", 98,
                "CheckedOut", 22,
                "AvailableRooms", 15
        );
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

package com.example.hotelreservationsystem.controller;

import com.example.hotelreservationsystem.model.Admin;
import com.example.hotelreservationsystem.model.Reservation;
import com.example.hotelreservationsystem.model.RoomType;
import com.example.hotelreservationsystem.utils.DatabaseUtil;
import com.example.hotelreservationsystem.utils.SceneSwitcher;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;


public class AdminController {

    @FXML private TableView<Reservation> bookingsTableView;
    @FXML private TableColumn<Reservation, String> nameTableColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkInTableColumn;
    @FXML private TableColumn<Reservation, LocalDate> checkOutTableColumn;
    @FXML private TableColumn<Reservation, Integer> guestsTableColumn;
    @FXML private TableColumn<Reservation, String> phoneTableColumn;
    @FXML private TableColumn<Reservation, String> statusTableColumn;
    @FXML private Button logOutBtn;
    @FXML private TextField nameTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField addressTextField;
    @FXML private TextField emailTextField;
    @FXML private ChoiceBox<Integer> guestsChoiceBox;
    @FXML private ChoiceBox<RoomType> roomTypeChoiceBox;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private TextField searchTextField;

    private Admin loggedInAdmin;
    private FilteredList<Reservation> filteredReservations;

    public void setLoggedInAdmin(Admin admin) {
        this.loggedInAdmin = admin;
    }

    @FXML
    private void initialize() {
        // populate guestsChoiceBox (1-4)
        guestsChoiceBox.getItems().addAll(IntStream.rangeClosed(1, 4).boxed().toList());

        // add listener to update roomTypeChoiceBox on change
        guestsChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateRoomTypeOptions(newVal);
            }
        });

        // preload existing columns setup
        checkInTableColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutTableColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        guestsTableColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        nameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getGuestName(cellData.getValue().getGuestID())));
        phoneTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getGuestPhone(cellData.getValue().getGuestID())));
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        ObservableList<Reservation> allReservations = getAllReservations();
        filteredReservations = new FilteredList<>(allReservations, p -> true);
        bookingsTableView.setItems(filteredReservations);

        // add search listener
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterBookings(newValue);
        });
    }

    private void filterBookings(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            filteredReservations.setPredicate(res -> true);
            return;
        }

        String lowerCaseKeyword = keyword.toLowerCase();

        filteredReservations.setPredicate(res -> {
            String guestName = getGuestName(res.getGuestID()).toLowerCase();
            String phone = getGuestPhone(res.getGuestID()).toLowerCase();

            return guestName.contains(lowerCaseKeyword) || phone.contains(lowerCaseKeyword);
        });
    }

    private void updateRoomTypeOptions(int numberOfGuests) {
        roomTypeChoiceBox.getItems().clear();

        List<String> allowedRoomTypes = getAllowedRoomTypes(numberOfGuests);

        Logger logger = Logger.getLogger(DatabaseUtil.class.getName());
        try (Connection conn = DatabaseUtil.getConnection()) {
            if (allowedRoomTypes.isEmpty()) return;

            String placeholders = String.join(",", Collections.nCopies(allowedRoomTypes.size(), "?"));
            String sql = "SELECT DISTINCT rt.TypeName " +
                    "FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE r.Status = 'AVAILABLE' AND rt.TypeName IN (" + placeholders + ")";
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int j = 0; j < allowedRoomTypes.size(); j++) {
                stmt.setString(j + 1, allowedRoomTypes.get(j));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roomTypeChoiceBox.getItems().add(RoomType.valueOf(rs.getString("TypeName")));
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred updating room types: " + e.getMessage(), e);
        }
    }

    private ObservableList<Reservation> getAllReservations() {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

        String query = "SELECT * FROM Reservation";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Reservation res = new Reservation();
                res.setReservationID(rs.getInt("ReservationID"));
                res.setGuestID(rs.getInt("GuestID"));
                res.setRoomID(rs.getInt("RoomID"));

                Date checkIn = rs.getDate("CheckInDate");
                Date checkOut = rs.getDate("CheckOutDate");
                res.setCheckInDate(checkIn != null ? checkIn.toLocalDate() : null);
                res.setCheckOutDate(checkOut != null ? checkOut.toLocalDate() : null);

                res.setNumberOfGuests(rs.getInt("NumberOfGuests"));
                res.setStatus(rs.getString("Status"));

                reservations.add(res);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred getting all reservations: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return reservations;
    }

    private String getGuestName(int guestId) {
        Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT Name FROM Guest WHERE GuestID = ?");
            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("Name");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred getting guest's name: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return "";
    }

    private String getGuestPhone(int guestId) {
        Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

        try (Connection conn = DatabaseUtil.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT PhoneNumber FROM Guest WHERE GuestID = ?");
            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("PhoneNumber");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred getting guest's phone number: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return "";
    }

    private double getRoomPrice(int roomID)  {
        double price = 0.0;
        Logger logger = Logger.getLogger(DatabaseUtil.class.getName());

        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT Price FROM Room WHERE RoomID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, roomID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                price = rs.getDouble("Price");
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "An error occurred getting room's price: " + e.getMessage(), e);
        }
        return price;
    }

    @FXML
    private void handleBookBtn(ActionEvent event){
        LocalDate checkInDate = checkInDatePicker.getValue();
        LocalDate checkOutDate = checkOutDatePicker.getValue();
        String guestName = nameTextField.getText();
        String phone = phoneTextField.getText();
        String address = addressTextField.getText();
        String email = emailTextField.getText();
        Integer guestNo = guestsChoiceBox.getValue();
        String roomType = String.valueOf(roomTypeChoiceBox.getValue());
        if (checkInDate == null || checkOutDate == null || guestName.isEmpty() || phone.isEmpty()
                || address.isEmpty() || email.isEmpty() || guestNo == null || roomType == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please fill in all fields.");
            alert.showAndWait();
            return;
        }
        Logger logger = Logger.getLogger(DatabaseUtil.class.getName());
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            // Create Guest
            String insertGuest = "INSERT INTO Guest(Name, PhoneNumber, Address, Email) VALUES (?, ?, ?, ?)";
            PreparedStatement guestStmt = conn.prepareStatement(insertGuest, Statement.RETURN_GENERATED_KEYS);
            guestStmt.setString(1, guestName);
            guestStmt.setString(2, phone);
            guestStmt.setString(3, address);
            guestStmt.setString(4, email);
            guestStmt.executeUpdate();

            ResultSet guestKeys = guestStmt.getGeneratedKeys();
            int guestID;
            if (guestKeys.next()) {
                guestID = guestKeys.getInt(1);
            } else {
                conn.rollback();
                throw new SQLException("Failed to retrieve guest ID.");
            }

            // Find available room of selected type
            String findRoom = "SELECT RoomID FROM Room r JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE rt.TypeName = ? AND r.Status = 'AVAILABLE' LIMIT 1";
            PreparedStatement roomStmt = conn.prepareStatement(findRoom);
            roomStmt.setString(1, roomType);
            ResultSet roomRs = roomStmt.executeQuery();

            int roomID;
            if (roomRs.next()) {
                roomID = roomRs.getInt("RoomID");
            } else {
                conn.rollback();
                Alert alert = new Alert(Alert.AlertType.ERROR, "No available rooms of selected type.");
                alert.showAndWait();
                return;
            }

            //create Reservation
            String insertRes = "INSERT INTO Reservation(GuestID, RoomID, CheckInDate, CheckOutDate, NumberOfGuests, Status) " +
                    "VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
            PreparedStatement resStmt = conn.prepareStatement(insertRes);
            resStmt.setInt(1, guestID);
            resStmt.setInt(2, roomID);
            resStmt.setDate(3, Date.valueOf(checkInDate));
            resStmt.setDate(4, Date.valueOf(checkOutDate));
            resStmt.setInt(5, guestNo);
            resStmt.executeUpdate();

            // Update Room Status
            String updateRoom = "UPDATE Room SET Status = 'OCCUPIED' WHERE RoomID = ?";
            PreparedStatement updateRoomStmt = conn.prepareStatement(updateRoom);
            updateRoomStmt.setInt(1, roomID);
            updateRoomStmt.executeUpdate();

            conn.commit();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Booking successfully created.");
            alert.showAndWait();
            ObservableList<Reservation> updatedReservations = getAllReservations();
            filteredReservations = new FilteredList<>(updatedReservations, p -> true);
            bookingsTableView.setItems(filteredReservations);

            // clear form
            clearForm();
            if(loggedInAdmin != null){
                Logger.getLogger(Admin.class.getName()).info("Booking successfully created by " + loggedInAdmin.getUsername());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "An error occurred crating booking: " + e.getMessage(), e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error while booking. See logs.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancelBtn(ActionEvent event){
        Reservation selectedReservation = bookingsTableView.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            showAlert("No Selection", "Please select a reservation first.");
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Cancellation");
        confirmation.setHeaderText("Cancel Reservation");
        confirmation.setContentText("Are you sure you want to cancel this reservation?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Logger logger = Logger.getLogger(DatabaseUtil.class.getName());
            try (Connection conn = DatabaseUtil.getConnection()) {
                String sql = "UPDATE Reservation SET Status = 'CANCELLED' WHERE ReservationID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selectedReservation.getReservationID());

                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    String updateRoomSQL = "UPDATE Room SET Status = 'AVAILABLE' WHERE RoomID = ?";
                    PreparedStatement roomStmt = conn.prepareStatement(updateRoomSQL);
                    roomStmt.setInt(1, selectedReservation.getRoomID());
                    roomStmt.executeUpdate();
                    roomStmt.close();
                    showAlert("Cancelled", "Reservation has been cancelled successfully.");

                    if(loggedInAdmin != null){
                        Logger.getLogger(Admin.class.getName()).info("Booking successfully cancelled by " + loggedInAdmin.getUsername());
                    }
                    selectedReservation.setStatus("CHECKED_OUT");
                    ObservableList<Reservation> updatedReservations = getAllReservations();
                    filteredReservations = new FilteredList<>(updatedReservations, p -> true);
                    bookingsTableView.setItems(filteredReservations);
                } else {
                    showAlert("Error", "Could not cancel the reservation.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, "An error occurred cancelling booking: " + e.getMessage(), e);
                showAlert("Database Error", "An error occurred while cancelling the reservation.");
            }
        }
    }


    private List<String> getAllowedRoomTypes(int guests) {
        if (guests <= 2) {
            return List.of("SINGLE", "DOUBLE", "DELUXE", "PENTHOUSE");
        } else if (guests <= 4) {
            return List.of("DOUBLE"); // limit to max 4 people
        } else {
            return new ArrayList<>();
        }
    }

    private void clearForm() {
        nameTextField.clear();
        phoneTextField.clear();
        addressTextField.clear();
        emailTextField.clear();
        guestsChoiceBox.setValue(null);
        roomTypeChoiceBox.setValue(null);
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        Reservation selectedReservation = bookingsTableView.getSelectionModel().getSelectedItem();

        if(selectedReservation != null) {
            try {
                // fetch extra info for billing
                String guestName = getGuestName(selectedReservation.getGuestID());
                double roomPrice = getRoomPrice(selectedReservation.getRoomID());

                // load the modal
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/hotelreservationsystem/bill-view.fxml"));
                Parent page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Guest Bill");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(bookingsTableView.getScene().getWindow());
                dialogStage.setScene(new Scene(page));

                BillController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setGuestName(guestName);
                controller.setRoomPrice(roomPrice);
                controller.setReservation(selectedReservation);


                dialogStage.showAndWait();

                if (controller.isConfirmed()) {
                    Logger logger = Logger.getLogger(DatabaseUtil.class.getName());
                    try (Connection conn = DatabaseUtil.getConnection()) {
                        String sql = "UPDATE Reservation SET Status = ? WHERE ReservationID = ?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setString(1, "CHECKED_OUT");
                        stmt.setInt(2, selectedReservation.getReservationID());

                        int rowsUpdated = stmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            selectedReservation.setStatus("CHECKED_OUT");

                            String updateRoomSQL = "UPDATE Room SET Status = 'AVAILABLE' WHERE RoomID = ?";
                            PreparedStatement roomStmt = conn.prepareStatement(updateRoomSQL);
                            roomStmt.setInt(1, selectedReservation.getRoomID());
                            roomStmt.executeUpdate();
                            roomStmt.close();

                            showAlert("Success", "Booking has been checked out!");
                            bookingsTableView.refresh();
                            if(loggedInAdmin != null){
                                Logger.getLogger(Admin.class.getName()).info("Booking successfully checked out by " + loggedInAdmin.getUsername());
                            }
                        }

                        stmt.close();
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "An error occurred checking out booking: " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLogOut(ActionEvent event) throws IOException {
        SceneSwitcher.switchToMainView(event);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

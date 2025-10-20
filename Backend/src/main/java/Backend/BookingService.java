package Backend;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class BookingService {

    public static String processManualBooking(JSONObject json) throws SQLException {
        String name = json.getString("name");
        String source = json.getString("source");
        String destination = json.getString("destination");
        String date = json.getString("date");
        int passengers = json.getInt("passengers");
        int farePerPassenger = json.getInt("farePerPassenger");
        
        int totalFare = farePerPassenger * passengers;
        
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO bookings (name, source, destination, date_of_journey, passengers, total_fare, bus_id) VALUES (?, ?, ?, ?, ?, ?, NULL)";
            PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, name);
            pst.setString(2, source);
            pst.setString(3, destination);
            pst.setDate(4, Date.valueOf(date));
            pst.setInt(5, passengers);
            pst.setInt(6, totalFare);
            pst.executeUpdate();
            
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                int bookingId = rs.getInt(1);
                JSONObject response = new JSONObject();
                response.put("bookingId", bookingId);
                response.put("totalFare", totalFare);
                response.put("message", "Booking Confirmed!");
                return response.toString();
            }
        }
        return ErrorHandler.createErrorResponse("Booking failed", 500);
    }

    public static String bookBus(JSONObject json) throws SQLException {
        String name = json.getString("name");
        String source = json.getString("source");
        String destination = json.getString("destination");
        String date = json.getString("date");
        int passengers = json.getInt("passengers");
        int busId = json.getInt("busId");
        
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Lock the row for update to prevent race conditions
                PreparedStatement pst = conn.prepareStatement("SELECT seats_available FROM bus_seats WHERE bus_id=? AND journey_date=? FOR UPDATE");
                pst.setInt(1, busId);
                pst.setDate(2, Date.valueOf(date));
                ResultSet rs = pst.executeQuery();
                
                if (rs.next() && rs.getInt("seats_available") >= passengers) {
                    int remainingSeats = rs.getInt("seats_available") - passengers;
                    
                    // Get fare
                    pst = conn.prepareStatement("SELECT fare_per_seat FROM buses WHERE bus_id=?");
                    pst.setInt(1, busId);
                    ResultSet rs2 = pst.executeQuery();
                    rs2.next();
                    int farePerSeat = rs2.getInt("fare_per_seat");
                    int totalFare = farePerSeat * passengers;
                    
                    // Create booking
                    pst = conn.prepareStatement(
                        "INSERT INTO bookings (name, source, destination, date_of_journey, passengers, total_fare, bus_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    pst.setString(1, name);
                    pst.setString(2, source);
                    pst.setString(3, destination);
                    pst.setDate(4, Date.valueOf(date));
                    pst.setInt(5, passengers);
                    pst.setInt(6, totalFare);
                    pst.setInt(7, busId);
                    pst.executeUpdate();
                    
                    // Get booking ID
                    ResultSet rs3 = pst.getGeneratedKeys();
                    int bookingId = 0;
                    if (rs3.next()) {
                        bookingId = rs3.getInt(1);
                    }
                    
                    // Update seats
                    pst = conn.prepareStatement("UPDATE bus_seats SET seats_available=? WHERE bus_id=? AND journey_date=?");
                    pst.setInt(1, remainingSeats);
                    pst.setInt(2, busId);
                    pst.setDate(3, Date.valueOf(date));
                    pst.executeUpdate();
                    
                    conn.commit();
                    
                    JSONObject response = new JSONObject();
                    response.put("bookingId", bookingId);
                    response.put("totalFare", totalFare);
                    response.put("message", "Booking Confirmed!");
                    return response.toString();
                } else {
                    conn.rollback();
                    return ErrorHandler.createErrorResponse("Not enough seats available", 409);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static String getUserBookings(JSONObject json) throws SQLException {
        String username = json.getString("username");
        List<JSONObject> bookings = new ArrayList<>();
        
        String sql = "SELECT * FROM bookings WHERE name = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                JSONObject booking = new JSONObject();
                booking.put("booking_id", rs.getInt("booking_id"));
                booking.put("name", rs.getString("name"));
                booking.put("source", rs.getString("source"));
                booking.put("destination", rs.getString("destination"));
                booking.put("date_of_journey", rs.getDate("date_of_journey").toString());
                booking.put("passengers", rs.getInt("passengers"));
                booking.put("total_fare", rs.getInt("total_fare"));
                booking.put("bus_id", rs.getObject("bus_id") != null ? rs.getInt("bus_id") : JSONObject.NULL);
                bookings.add(booking);
            }
        }
        
        return new JSONArray(bookings).toString();
    }
}

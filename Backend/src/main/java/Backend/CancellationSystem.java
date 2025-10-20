package Backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class CancellationSystem {

    public static String cancelBooking(JSONObject json) throws SQLException {
        int bookingId = json.getInt("bookingId");

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Get booking details
                PreparedStatement pst = conn.prepareStatement("SELECT bus_id, passengers, date_of_journey FROM bookings WHERE booking_id=?");
                pst.setInt(1, bookingId);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int busId = rs.getInt("bus_id");
                    int passengers = rs.getInt("passengers");
                    String date = rs.getDate("date_of_journey").toString();

                    // Update seats
                    pst = conn.prepareStatement("UPDATE bus_seats SET seats_available = seats_available + ? WHERE bus_id=? AND journey_date=?");
                    pst.setInt(1, passengers);
                    pst.setInt(2, busId);
                    pst.setDate(3, java.sql.Date.valueOf(date));
                    pst.executeUpdate();

                    // Delete booking
                    pst = conn.prepareStatement("DELETE FROM bookings WHERE booking_id=?");
                    pst.setInt(1, bookingId);
                    pst.executeUpdate();

                    conn.commit();

                    JSONObject response = new JSONObject();
                    response.put("message", "Booking Cancelled!");
                    return response.toString();
                } else {
                    conn.rollback();
                    return ErrorHandler.createErrorResponse("Booking not found", 404);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}

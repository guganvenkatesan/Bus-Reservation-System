package Backend;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class SeatBooking {

    public static String bookSeat(JSONObject json) throws SQLException {
        int busId = json.getInt("busId");
        String date = json.getString("date");
        int seatsToBook = json.getInt("seats");
        String passengerName = json.getString("passengerName");

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Check seat availability
                PreparedStatement pst = conn.prepareStatement("SELECT seats_available FROM bus_seats WHERE bus_id=? AND journey_date=?");
                pst.setInt(1, busId);
                pst.setDate(2, Date.valueOf(date));
                ResultSet rs = pst.executeQuery();

                if (rs.next() && rs.getInt("seats_available") >= seatsToBook) {
                    int remainingSeats = rs.getInt("seats_available") - seatsToBook;

                    // Get fare
                    pst = conn.prepareStatement("SELECT fare_per_seat FROM buses WHERE bus_id=?");
                    pst.setInt(1, busId);
                    ResultSet rs2 = pst.executeQuery();
                    rs2.next();
                    int farePerSeat = rs2.getInt("fare_per_seat");
                    int totalFare = farePerSeat * seatsToBook;

                    // Create booking
                    pst = conn.prepareStatement(
                        "INSERT INTO bookings (name, source, destination, date_of_journey, passengers, total_fare, bus_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    pst.setString(1, passengerName);
                    pst.setString(2, json.getString("source"));
                    pst.setString(3, json.getString("destination"));
                    pst.setDate(4, Date.valueOf(date));
                    pst.setInt(5, seatsToBook);
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
}

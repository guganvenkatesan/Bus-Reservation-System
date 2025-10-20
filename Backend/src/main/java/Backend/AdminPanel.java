package Backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminPanel {

    public static String getBookings(JSONObject json) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM bookings";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            JSONArray bookings = new JSONArray();
            while (rs.next()) {
                JSONObject booking = new JSONObject();
                booking.put("booking_id", rs.getInt("booking_id"));
                booking.put("name", rs.getString("name"));
                booking.put("source", rs.getString("source"));
                booking.put("destination", rs.getString("destination"));
                booking.put("date_of_journey", rs.getDate("date_of_journey").toString());
                booking.put("passengers", rs.getInt("passengers"));
                booking.put("total_fare", rs.getInt("total_fare"));
                booking.put("bus_id", rs.getInt("bus_id"));
                bookings.put(booking);
            }
            return bookings.toString();
        }
    }
}

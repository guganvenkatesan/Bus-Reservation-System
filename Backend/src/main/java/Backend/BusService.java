package Backend;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class BusService {

    public static String searchBuses(JSONObject json) throws SQLException {
        String source = json.getString("source");
        String destination = json.getString("destination");
        String date = json.getString("date");
        int passengers = json.getInt("passengers");
        
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT b.bus_id, b.name, b.departure, b.arrival, b.fare_per_seat, bs.seats_available " +
                         "FROM buses b JOIN bus_seats bs ON b.bus_id = bs.bus_id " +
                         "WHERE b.source=? AND b.destination=? AND bs.journey_date=? AND bs.seats_available>=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, source);
            pst.setString(2, destination);
            pst.setDate(3, Date.valueOf(date));
            pst.setInt(4, passengers);
            
            ResultSet rs = pst.executeQuery();
            JSONArray buses = new JSONArray();
            
            while (rs.next()) {
                JSONObject bus = new JSONObject();
                bus.put("bus_id", rs.getInt("bus_id"));
                bus.put("name", rs.getString("name"));
                bus.put("departure", rs.getString("departure"));
                bus.put("arrival", rs.getString("arrival"));
                bus.put("fare_per_seat", rs.getInt("fare_per_seat"));
                bus.put("seats_available", rs.getInt("seats_available"));
                buses.put(bus);
            }
            
            return buses.toString();
        }
    }
}

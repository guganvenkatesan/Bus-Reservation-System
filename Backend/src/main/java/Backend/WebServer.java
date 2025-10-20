package Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class WebServer {

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(Config.getServerPort()), 0);
            
            // Enable CORS
        server.createContext("/api/manual-booking", new ManualBookingHandler());
        server.createContext("/api/search-buses", new SearchBusesHandler());
        server.createContext("/api/book-bus", new BookBusHandler());
        server.createContext("/api/book-seat", new SeatBookingHandler());
        server.createContext("/api/cancel-booking", new CancellationHandler());
        server.createContext("/api/admin/bookings", new AdminBookingsHandler());
        server.createContext("/api/user/profile", new UserProfileHandler());
        server.createContext("/api/user-bookings", new UserBookingsHandler());
        
            server.setExecutor(null);
            server.start();
            System.out.println("Server started on port " + Config.getServerPort());
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
    
    // Abstract base handler to reduce boilerplate
    static abstract class BaseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendCORS(exchange);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = readRequestBody(exchange);
                    JSONObject json = new JSONObject(requestBody);
                    String result = processRequest(json);
                    sendResponse(exchange, result, 200);
                } catch (JSONException e) {
                    sendResponse(exchange, ErrorHandler.createErrorResponse("Invalid JSON format or missing fields.", 400), 400);
                } catch (Exception e) {
                    e.printStackTrace(); // Log the full exception
                    sendResponse(exchange, ErrorHandler.createErrorResponse("Internal server error.", 500), 500);
                }
            } else {
                sendResponse(exchange, ErrorHandler.createErrorResponse("Method not allowed", 405), 405);
            }
        }
        
        protected abstract String processRequest(JSONObject json) throws Exception;
    }

    static class ManualBookingHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return BookingService.processManualBooking(json);
        }
    }
    
    static class SearchBusesHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return BusService.searchBuses(json);
        }
    }
    
    static class BookBusHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return BookingService.bookBus(json);
        }
    }

    static class SeatBookingHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return SeatBooking.bookSeat(json);
        }
    }

    static class CancellationHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return CancellationSystem.cancelBooking(json);
        }
    }

    static class AdminBookingsHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return AdminPanel.getBookings(json);
        }
    }

    static class UserProfileHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return UserProfile.getUserProfile(json);
        }
    }

    static class UserBookingsHandler extends BaseHandler {
        @Override
        protected String processRequest(JSONObject json) throws Exception {
            return BookingService.getUserBookings(json);
        }
    }
    
    // Helper methods
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Add CORS header
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    private static void sendCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1); // No Content for CORS preflight
    }
}

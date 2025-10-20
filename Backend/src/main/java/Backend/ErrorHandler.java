package Backend;

import org.json.JSONObject;

public class ErrorHandler {
    public static String createErrorResponse(String message, int statusCode) {
        JSONObject error = new JSONObject();
        error.put("error", message);
        error.put("statusCode", statusCode);
        return error.toString();
    }
}

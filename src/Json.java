/*
 *  src/Json.java
 *
 *  Json support functions.
 */

import org.json.*;

import java.net.*;
import java.util.*;

public class Json {

    /**
     * Get the string value at the given key.
     * @return Null if the element was not found.
     */
    static String string(JSONObject obj, String key) {
        return Util.isNull(obj) ? null : obj.optString(key);
    }

    /**
     * Get the object at the given key.
     * @return Null if the element was not found.
     */
    static JSONObject object(JSONObject obj, String key) {
        return Util.isNull(obj) ? null : obj.optJSONObject(key);
    }

    /**
     * Get the array at the given key.
     * @return An empty array if the element was not found.
     */
    static JSONArray array(JSONObject obj, String key) {
        JSONArray result = Util.isNull(obj) ? null : obj.optJSONArray(key);
        return (result == null) ? new JSONArray() : result;
    }

    /**
     * Generate a JSON object containing { "error": message }
     */
    static JSONObject error(Exception e) {
        return error(e.getMessage());
    }

    /**
     * Generate a JSON object containing { "error": message }
     */
    static JSONObject error(String message) {
        Map<String,Object> map = new HashMap<>();
        map.put("error", message);
        return new JSONObject(map);
    }

    /**
     * Get the JSON object from the given URL.
     */
    static JSONObject fetch(String url) throws Exception {
        return fetch(Util.makeURL(url));
    }

    /**
     * Get the JSON object from the given URL.
     */
    static JSONObject fetch(URL url) throws Exception {
        if (url == null) {
            throw new Exception("missing URL");
        }
        HttpURLConnection conn = null;
        try {
            conn = Http.apiGet(url);
            int status = conn.getResponseCode();
            if ((status < 200) || (status >= 300)) {
                throw new Exception("HTTP " + status + " returned by " + url);
            }
            String text = Http.getContent(conn);
            return new JSONObject(text);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}

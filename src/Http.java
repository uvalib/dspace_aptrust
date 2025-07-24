/*
 *  src/Http.java
 *
 *  Network connection support functions.
 */

import java.io.*;
import java.net.*;
import java.nio.charset.*;

public class Http {

    /**
     * Open an HTTP connection to an API endpoint that returns JSON.
     */
    static HttpURLConnection apiGet(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    /**
     * Acquire content from an open network connection.
     */
    static String getContent(HttpURLConnection conn) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = getReader(conn)) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    /**
     * Get an input stream reader.
     */
    static BufferedReader getReader(HttpURLConnection conn) throws IOException {
        InputStream       s = conn.getInputStream();
        InputStreamReader r = new InputStreamReader(s, StandardCharsets.UTF_8);
        return new BufferedReader(r);
    }

}

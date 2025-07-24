/*
 *  src/Util.java
 *
 *  General utility functions.
 */

import org.json.*;

import java.net.*;
import java.nio.charset.*;
import java.util.*;

public class Util {

    /**
     * Indicate whether a value is null.
     */
    static boolean isNull(Object value) {
        return (value == null) || JSONObject.NULL.equals(value);
    }

    /**
     * Indicate whether a value is null or empty.
     * (NOTE: boolean "false" is _not_ considered empty.)
     */
    @SuppressWarnings("unchecked")
    static boolean isBlank(Object value) {
        if (value instanceof String) {
            return ((String) value).isEmpty();
        } else if (value instanceof JSONArray) {
            return ((JSONArray) value).isEmpty();
        } else if (value instanceof JSONObject) {
            return ((JSONObject) value).isEmpty();
        } else if (value instanceof Collection) {
            return ((Collection<Object>) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map<Object,Object>) value).isEmpty();
        } else {
            return isNull(value);
        }
    }

    /**
     * Return the URL with no additional parameters.
     */
    static URL makeURL(String url) throws MalformedURLException {
        return new URL(url);
    }

    /**
     * Return the URL with the provided parameters added.
     */
    static URL makeURL(String url, Map<String,Object> prm) throws MalformedURLException {
        StringBuilder sb = new StringBuilder(url);
        boolean first = !url.contains("?");
        for (Map.Entry<String,Object> entry : prm.entrySet()) {
            String value = entry.getValue().toString();
            sb.append(first ? "?" : "&");
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            first = false;
        }
        return new URL(sb.toString());
    }

    /**
     * Append one or more key=value query parameters to the source URL.
     */
    static URL appendParameter(URL url, String... addedQueries) throws MalformedURLException {
        if (addedQueries.length == 0) return url;
        String  base  = url.toString();
        int     qIdx  = base.indexOf("?");
        String  path  = (qIdx >= 0) ? base.substring(0, qIdx) : base;
        String  query = (qIdx >= 0) ? base.substring(qIdx)    : "";
        boolean first = query.isEmpty();
        for (String q : addedQueries) {
            query += first ? "?" : "&";
            query += q;
            first = false;
        }
        return new URL(path + query);
    }

}

/*
 *  src/APTrustServlet.java
 *
 *  This webapp provides metadata and content links for DSpace items over a date
 *  range provided through URL parameters.
 *
 *  A Solr query is performed to get the UUIDs of items added during the given
 *  date range.  The DSpace API is called on each UUID to retrieve the item's
 *  metadata and links to its content.
 */

import org.json.*;

import java.io.*;
import java.nio.charset.*;
import java.time.*;
import java.time.format.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class APTrustServlet extends HttpServlet {

    static final String START_PARAM   = "start_date";
    static final String END_PARAM     = "end_date";
    static final String DEFAULT_START = "2000-01-01";
    static final String INVALID_DATE  = "invalid format; use YYYY-MM-DD";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        try {
            Instant   startTime = getStartTime(request);
            Instant   endTime   = getEndTime(request);
            JSONArray entries   = DSpace.getEntries(startTime, endTime);
            try (OutputStream out = response.getOutputStream()) {
                out.write(entries.toString().getBytes(StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Json.error(e).toString());
        }
    }

    /**
     * Get the START_PARAM URL parameter value.
     * @return The beginning of DEFAULT_START if no parameter was given.
     */
    static Instant getStartTime(HttpServletRequest request) throws ParamException {
        Instant result = null;
        String  value  = request.getParameter(START_PARAM);
        try {
            if (Util.isBlank(value)) {
                value = DEFAULT_START;
            }
            LocalDate day = LocalDate.parse(value);
            result = day.atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            throw new ParamException(START_PARAM, value, INVALID_DATE);
        }
        return result;
    }

    /**
     * Get the END_PARAM URL parameter value.
     * @return The end of today if no parameter was given.
     */
    static Instant getEndTime(HttpServletRequest request) throws ParamException {
        Instant result = null;
        String  value  = request.getParameter(END_PARAM);
        try {
            LocalDate day;
            if (Util.isBlank(value)) {
                day = LocalDate.now(ZoneOffset.UTC);
            } else {
                day = LocalDate.parse(value);
            }
            result = day.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new ParamException(END_PARAM, value, INVALID_DATE);
        }
        return result;
    }

    /**
     * Raised to indicate a bad URL parameter value.
     */
    private static class ParamException extends Exception {
        public ParamException(String key, String value, String message) {
            super(key + ": '" + value + "': " + message);
        }
    }

}

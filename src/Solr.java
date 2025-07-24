/*
 *  src/Solr.java
 *
 *  Solr query support functions.
 */

import java.time.*;
import java.time.format.*;

public class Solr {

    /**
     * Render a date range in the form expected by Solr.
     */
    static String range(Instant startTime, Instant endTime) {
        return "[" + date(startTime) + " TO " + date(endTime) + "]";
    }

    /**
     * Render a datetime value in the form expected by Solr.
     */
    static String date(Instant time) {
        return DateTimeFormatter.ISO_INSTANT.format(time);
    }

}

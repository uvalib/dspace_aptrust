/*
 *  src/DSpace.java
 *
 *  Recursively acquire metadata and content links for a DSpace item.
 */

import org.json.*;

import java.net.*;
import java.time.*;
import java.util.*;

public class DSpace {

    static final String API_HOST  = "dspace-staging.library.virginia.edu";
    static final String API_BASE  = "https://" + API_HOST + "/server/api";
    static final String API_QUERY = API_BASE + "/discover/search/objects";

    static final int ROWS = 100; // NOTE: This is all DSpace seems to allow.

    /**
     * Return the DSpace API URL for all items for the date range.
     */
    static URL itemsUrl(Instant startTime, Instant endTime) throws MalformedURLException {
        Map<String,Object> prm = new HashMap<>();
        prm.put("query",   "lastModified:" + Solr.range(startTime, endTime));
        prm.put("dsoType", "item");
        prm.put("size",    ROWS);
        return Util.makeURL(API_QUERY, prm);
    }

    /**
     * Generate a list entries for DSpace item submitted within the date range.
     * 
     * Because DSpace limits the number of items that can be returned, this
     * function makes repeated queries until a query returns with empty results.
     */
    static JSONArray getEntries(Instant startTime, Instant endTime) throws Exception {
        JSONArray result  = new JSONArray();
        URL       baseUrl = itemsUrl(startTime, endTime);
        boolean   getMore = true;
        for (int page = 0; getMore; page++) {
            URL        url   = Util.appendParameter(baseUrl, "page=" + page);
            JSONObject json  = Json.fetch(url);
            JSONArray  items = getEntries(json);
            if (items.isEmpty()) {
                getMore = false;
            } else {
                result.putAll(items);
            }
        }
        return result;
    }

    /**
     * Generate a list of entries from DSpace search results.
     */
    static JSONArray getEntries(JSONObject json) throws Exception {
        JSONArray  result = new JSONArray();
        JSONObject embed1 = Json.object(json, "_embedded");
        JSONObject search = Json.object(embed1, "searchResult");
        JSONObject embed2 = Json.object(search, "_embedded");
        for (Object obj : Json.array(embed2, "objects")) {
            JSONObject embed = Json.object((JSONObject)obj, "_embedded");
            JSONObject data  = Json.object(embed, "indexableObject");
            JSONObject entry = makeEntry(data);
            result.put(entry);
        }
        return result;
    }

    /**
     * Generate data for a webapp response entry for the DSpace item including
     * item metadata and content links.
     */
    static JSONObject makeEntry(JSONObject itemJson) throws Exception {

        // Initialize entry components.
        Map<String,Object> metadataValues = new HashMap<>();
        List<Object>       contentLinks   = new ArrayList<>();

        // Gather item metadata.
        String bundlesUrl = null;
        for (String key : itemJson.keySet()) {
            if (key.equals("_links")) {
                JSONObject links   = Json.object(itemJson, key);
                JSONObject bundles = Json.object(links, "bundles");
                bundlesUrl = Json.string(bundles, "href");
            } else {
                metadataValues.put(key, itemJson.get(key));
            }
        }

        // Fetch content links.
        JSONObject bundlesJson = Json.fetch(bundlesUrl);
        JSONObject bundlesInfo = Json.object(bundlesJson, "_embedded");
        for (Object bundle : Json.array(bundlesInfo, "bundles")) {
            JSONObject bundleObj = (JSONObject) bundle;
            JSONObject linksObj  = Json.object(bundleObj, "_links");
            JSONObject bsObj     = Json.object(linksObj, "bitstreams");
            String     bsUrl     = Json.string(bsObj, "href");
            JSONObject bsJson    = Json.fetch(bsUrl);
            JSONObject bsInfo    = Json.object(bsJson, "_embedded");
            for (Object bitstream : Json.array(bsInfo, "bitstreams")) {
                JSONObject bObj     = (JSONObject) bitstream;
                String     bName    = Json.string(bObj, "name");
                String     bKind    = Json.string(bObj, "bundleName");
                JSONObject bLinks   = Json.object(bObj, "_links");
                JSONObject bContent = Json.object(bLinks, "content");
                String     bUrl     = Json.string(bContent, "href");
                Map<String,Object> entry = new HashMap<>();
                entry.put("name", bName);
                entry.put("kind", bKind);
                entry.put("link", bUrl);
                contentLinks.add(entry);
            }
        }

        // Generate the entry.
        Map<String,Object> map = new HashMap<>();
        map.put("item",    metadataValues);
        map.put("content", contentLinks);
        return new JSONObject(map);
    }

}

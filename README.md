# APTrust Data Endpoint for DSpace

This webapp implements a Solr query to extract metadata for DSpace items within
a range of dates which can be used by existing UVALIB infrastructure for storing
in APTrust.

Although intended to be installed on the Tomcat of the DSpace server, it should
work from a Tomcat running on any host inside the UVA VPN.

## Scripts

Use `bin/make_war` to generate "aptrust.war" from compiled Java sources.
This can be installed in the local Tomcat or remote-copied to DSpace.

The `bin/test_war` regenerates "aptrust.war" and installs it in the local
Tomcat, allowing for one-step testing of changes to the code.
Tomcat is stopped and restarted in order to start with fresh logs.

The `bin/deploy_war` regenerates "aptrust.war" and installs it in the remote
DSpace Tomcat.
Tomcat is not restarted because the webapp is marked as reloadable;
this allows updates to the webapp without affecting the running DSpace instance.

## Usage

When properly installed, this webapp provides an "/aptrust" endpoint which
accepts optional "start_date" and "end_date" URL parameters and responds with
a JSON array of metadata objects for each matching DSpace entry.

|URL path|Emits DSpace entries...|
|-|-|
|/aptrust|for all time|
|/aptrust?start_date=2025-06-01|since June 1, 2025|
|/aptrust?end_date=2025-06-18|through June 18, 2025|
|/aptrust?start_date=2025-06-01&end_date=2025-06-18|between June 1 and June 18, 2025, inclusive|

## Output

The body of the response is a JSON array with a JSON object for each DSpace item
created within the given date range (inclusive).

Each DSpace item entry object has this structure:

```
    {
        "item": {
            "id":           UUID,
            "uuid":         UUID,
            "handle":       string,
            "name":         string,
            "type":         string,
            "entityType":   string,
            "inArchive":    boolean,
            "withdrawn":    boolean,
            "discoverable": boolean,
            "lastModified": date,
            "metadata": {
                type1: [ { "value": string, ... } ],
                type2: [ { "value": string, ... } ],
                ...
            },
        }
        "content": [
            { "name": string, "kind": string, "link": URL },
            { "name": string, "kind": string, "link": URL },
            ...
        ]
    }
```

## Requirements

This is built with Java OpenJDK 17 and generates a webapp for Tomcat 10.

To use the deploy_war script, the scripts of `dspace_util` must be available on
the local machine at the location indicated in `bin/values`.
(Export environment variable UTIL_BIN with the path to the directory, or edit
`bin/values` as appropriate.)

## Implementation Details

A Solr query is performed to get the UUIDs of items added during the given date
date range.
For each item UUID, the DSpace API is used to retrieve its metadata and links to
further API endpoints to retreive the content links of its component bitstreams
(i.e. content files).


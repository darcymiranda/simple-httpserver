package me.dmiranda.httpserver;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by dmiranda on 7/30/2014.
 */

public class HttpHeader {

    private String method;
    private String location;
    private String version;
    private HashMap<String, String> fields = new HashMap<String, String>();

    public HttpHeader(String method, String location, String version){

        this.method = method.toUpperCase();
        this.location = location;
        this.version = version;
    }

    public HttpHeader(){
        setDefaults();
    }

    public void setDefaults(){
        version = "HTTP/1.1";
        fields.put("server", "simple-httpserver");
        fields.put("date", new Date().toString());
    }

    public boolean isValid(){
        return method != null && location != null && version != null;
    }

    public String getMethod() {
        return method;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMethod(String method) {
        this.method = method.toUpperCase();
    }

    public HashMap<String, String> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "Method: " + method + ", Location: " + location + ", Version: " + version + ", Field Count: " + fields.size();
    }
}

package me.dmiranda.httpserver;

/**
 * Created by dmiranda on 7/28/2014.
 */
public class HttpRequest {

    private HttpMessage message;

    public HttpRequest(HttpMessage message){

        this.message = message;

    }

    public HttpMessage getMessage() {
        return message;
    }
}

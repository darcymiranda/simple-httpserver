package me.dmiranda.httpserver;

/**
 * Created by dmiranda on 7/28/2014.
 */
public class HttpResponse {

    private HttpMessage message;
    private HttpResponseCode responseCode;

    public HttpResponse(HttpMessage message, HttpResponseCode responseCode){

        this.message = message;
        this.responseCode = responseCode;
    }

    public HttpResponseCode getResponseCode() {
        return responseCode;
    }

    public HttpMessage getMessage() {
        return message;
    }

    public String getResponseHeader(){
        return message.getHeader().getVersion() + " " + responseCode;
    }

    static class HttpResponseCode {

        static final HttpResponseCode OK = new HttpResponseCode("OK", 100);
        static final HttpResponseCode NOT_FOUND = new HttpResponseCode("Not Found", 404);

        public String status;
        public int code;

        public HttpResponseCode(String status, int code){

            this.status = status;
            this.code = code;
        }

        @Override
        public String toString(){
            return code + " " + status;
        }
    }
}

package me.dmiranda.httpserver;

// TODO: Probably should just let Response/Request classes handle this crap...?

/**
 * Created by dmiranda on 7/30/2014.
 */
public class HttpMessage {

    private HttpHeader header;
    private byte[] data;

    public HttpMessage(HttpHeader header, byte[] data){

        this.header = header;
        this.data = data;

        header.getFields().put("content-length", Integer.toString(data.length));
    }

    public HttpMessage(byte[] data){
        this(new HttpHeader(), data);
    }

    public HttpMessage(){
        this(new HttpHeader(), new byte[0]);
    }

    @Override
    public String toString() {
        return header + " Content Length: " + Integer.toString(data != null ? data.length : 0);
    }

    public HttpHeader getHeader(){
        return header;
    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] data){ this.data = data; }
}

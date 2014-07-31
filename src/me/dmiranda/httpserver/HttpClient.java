package me.dmiranda.httpserver;

import me.dmiranda.httpserver.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.Map;

/**
 * Created by dmiranda on 7/28/2014.
 */
public class HttpClient {

    private final SocketChannel channel;
    private ByteBuffer buffer;

    public HttpClient(SocketChannel channel){

        this.channel = channel;
        buffer = ByteBuffer.allocate(4096);
    }

    public HttpRequest readRequest() throws IOException, HttpMessageException {

        buffer.clear();
        if(channel.read(buffer) == -1){
            return null;
        }

        buffer.flip();

        String rawData;
        try{
            rawData = HttpServer.UTF8_DECODER.decode(buffer).toString();
        } catch(CharacterCodingException e){
            Log.debug("HttpMessage", "Could not decode message with UTF-8" + buffer);
            throw new HttpMessageException("Not an HTTP message. Could not decode data");
        }

        HttpHeader httpHeader;
        String[] fields = rawData.split("\r\n");
        if(rawData.length() > 0){

            String[] values = fields[0].split(" ");
            if(values.length == 3){

                httpHeader = new HttpHeader(values[0], values[1], values[2]);

            } else {
                throw new HttpMessageException("Invalid HTTP header");
            }
        } else {
            throw new HttpMessageException("Not an HTTP message");
        }

        if(!httpHeader.isValid()){
            throw new HttpMessageException("Bad header segment. Got " + httpHeader);
        }

        if(rawData.length() > 1) {
            for (int i = 1; i < fields.length; i++) {

                String[] values = fields[i].split(": ");
                if (values.length == 2) {
                    httpHeader.getFields().put(values[0], values[1]);
                }
            }
        }

        if(httpHeader.getMethod().equals("POST")){
            if(fields[fields.length - 1].equals("\r\n")){
                Log.debug("POST DATA", fields[fields.length]);
            }
        }

        HttpMessage message = new HttpMessage(httpHeader, new byte[0]);

        //Log.debug("Received", message.toString());

        return new HttpRequest(message);
    }

    public void sendResponse(HttpResponse response) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(response.getResponseHeader() + "\n");
        for(Map.Entry<String, String> header : response.getMessage().getHeader().getFields().entrySet()){
            sb.append(header.getKey() + ": " + header.getValue() + "\n");
        }
        sb.append("\n");

        channel.write(HttpServer.UTF8_ENCODER.encode(CharBuffer.wrap(sb)));
        channel.write(ByteBuffer.wrap(response.getMessage().getData()));
        //Log.debug("Sent", sb.toString());
    }

    public void close() throws IOException {
        channel.close();
    }

    public SocketChannel getSocketChannel(){ return channel; }
}
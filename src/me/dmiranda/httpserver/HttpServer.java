package me.dmiranda.httpserver;


import me.dmiranda.httpserver.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Created by dmiranda on 7/28/2014.
 */
public class HttpServer implements Runnable {

    static final CharsetEncoder UTF8_ENCODER = Charset.forName("UTF-8").newEncoder();
    static final CharsetDecoder UTF8_DECODER = Charset.forName("UTF-8").newDecoder();

    private final ServerSocketChannel serverSocket;
    private final Selector selector;

    private final String ROOT_WEBSITE_DIR = "website";

    public HttpServer(int port) throws IOException {

        serverSocket = ServerSocketChannel.open();
        selector = Selector.open();

        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        Log.info("Server", "Started on port " + port);
    }

    @Override
    public void run(){
        try{

            selector.selectNow();
            for(Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();){

                SelectionKey key = it.next();
                if(!key.isValid()) continue;

                try{

                    if(key.isAcceptable()){

                        SocketChannel channel = serverSocket.accept();
                        if(channel == null) continue;

                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        Log.debug("Socket", "Connected " + channel.getRemoteAddress());

                    } else if(key.isReadable()){

                        HttpClient client = (HttpClient) key.attachment();
                        if(client == null){
                            client = new HttpClient((SocketChannel) key.channel());
                            key.attach(client);
                        }

                        HttpRequest request = client.readRequest();
                        if(request != null){

                            Log.info("Http", request.getMessage().getHeader().getMethod() + " "
                                    + request.getMessage().getHeader().getLocation() +
                                    " for " + client.getSocketChannel().getRemoteAddress());

                            HttpResponse response = createResponse(request.getMessage());
                            client.sendResponse(response);

                            Log.info("Http", "Sent " + response.getResponseCode().toString());

                        }

                        client.close();

                    } else if(key.isWritable()){

                    }

                }catch(Exception e){
                    Log.error("Server", e);
                    if(key.attachment() != null){
                        ((HttpClient)key.attachment()).close();
                    }
                }
            }

        }catch(IOException e){
            Log.error("Server", e);
            close();
        }
    }

    private HttpResponse createResponse(HttpMessage message) throws IOException {

        Path resourcePath = Paths.get(ROOT_WEBSITE_DIR + message.getHeader().getLocation());

        if(resourcePath.toString().contains("..")){
            return new HttpResponse(new HttpMessage(), HttpResponse.HttpResponseCode.NOT_FOUND);
        }

        if(resourcePath.toString().equals(ROOT_WEBSITE_DIR)){
            resourcePath = Paths.get(resourcePath + "/index.html");
        }
        else if(Paths.get(resourcePath + ".html").toFile().exists()){
            resourcePath = Paths.get(resourcePath + ".html");
        }
        else if(!resourcePath.toFile().exists()){

            if(Paths.get(ROOT_WEBSITE_DIR + "/404.html").toFile().exists()) {
                resourcePath = Paths.get(ROOT_WEBSITE_DIR + "/404.html");
            }
            else{
                return new HttpResponse(new HttpMessage(), HttpResponse.HttpResponseCode.NOT_FOUND);
            }
        }

        return new HttpResponse(new HttpMessage(Files.readAllBytes(resourcePath)), HttpResponse.HttpResponseCode.OK);
    }

    public void close(){
        try {
            selector.close();
            serverSocket.close();
        }catch(Exception e){
            // Do nothing
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException {

        Log.set(Log.INFO);
        HttpServer server = new HttpServer(args.length > 0 ? Integer.parseInt(args[0]) : 80);

        new File(server.ROOT_WEBSITE_DIR).mkdir();
        File index = new File(server.ROOT_WEBSITE_DIR + "/index.html");
        if(!index.exists()){
            BufferedWriter bw = new BufferedWriter(new FileWriter(index));
            bw.write("<h1>Hello World</h1>");
            bw.close();
        }

        for(;;){
            server.run();
            Thread.sleep(100);
        }
    }
}

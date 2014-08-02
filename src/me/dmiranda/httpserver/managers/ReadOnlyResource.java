package me.dmiranda.httpserver.managers;

import java.nio.ByteBuffer;

/**
 * Created by dmiranda on 8/2/2014.
 */
public class ReadOnlyResource {

    private ByteBuffer buffer;

    public ReadOnlyResource(ByteBuffer buffer){
        this.buffer = buffer;

    }

    public byte[] read(){
        byte[] bytes = buffer.array();
        buffer.rewind();
        return bytes;
    }

}

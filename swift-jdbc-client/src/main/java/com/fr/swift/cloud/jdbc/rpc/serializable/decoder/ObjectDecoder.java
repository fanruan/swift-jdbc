package com.fr.swift.cloud.jdbc.rpc.serializable.decoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author yee
 * @date 2018/8/26
 */
public class ObjectDecoder extends AbstractSerializableDecoder {
    private static final int STEP = 1024;

    @Override
    public Object decode(byte[] bytes) throws Exception {
        try (ObjectInputStream oos = createObjectInputStream(bytes)) {
            return oos.readObject();
        }
    }

    @Override
    public Object decodeFromChannel(SocketChannel channel) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(STEP);
        int length = 0;
        while ((length = channel.read(buffer)) > 0) {
            buffer.flip();
            bos.write(buffer.array(), 0, length);
            buffer.clear();
            if (length < STEP - 1) {
                break;
            }
        }
        return decode(bos.toByteArray());
    }

    protected ObjectInputStream createObjectInputStream(byte[] bytes) throws IOException {
        return new ObjectInputStream(new ByteArrayInputStream(bytes));
    }
}

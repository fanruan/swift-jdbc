package com.fr.swift.jdbc.rpc.util;

import com.fr.swift.log.SwiftLoggers;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;

/**
 * @author yee
 * @date 2019-09-05
 */
public class SocketChannelUtils {
    public static <Channel extends NetworkChannel> Channel wrapSocketOptions(Channel channel, int bufferSize) {
        try {
            return (Channel) channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE)
                    .setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE)
                    .setOption(StandardSocketOptions.SO_RCVBUF, bufferSize)
                    .setOption(StandardSocketOptions.SO_SNDBUF, bufferSize);

        } catch (IOException e) {
            SwiftLoggers.getLogger().warn(e);
            return channel;
        }
    }

}
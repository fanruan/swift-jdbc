package com.fr.swift.cloud.jdbc.rpc.selector;

import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.jdbc.exception.NoCodecResponseException;
import com.fr.swift.cloud.jdbc.rpc.connection.RpcNioConnector;
import com.fr.swift.cloud.jdbc.rpc.invoke.BaseSelector;
import com.fr.swift.cloud.jdbc.rpc.serializable.decoder.SerializableDecoder;
import com.fr.swift.cloud.jdbc.rpc.serializable.encoder.SerializableEncoder;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author yee
 * @date 2018/8/26
 */
public class RpcNioSelector extends BaseSelector<RpcNioConnector> {
    private static final int READ_OP = SelectionKey.OP_READ;
    private static final int READ_WRITE_OP = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    private final SelectionThread thread = new SelectionThread();
    private Selector selector;
    private LinkedList<Runnable> selectTasks = new LinkedList<Runnable>();
    private ConcurrentHashMap<SocketChannel, RpcNioConnector> connectorCache;
    private List<RpcNioConnector> connectors;
    private AtomicBoolean started = new AtomicBoolean(false);
    private SerializableEncoder encoder;
    private SerializableDecoder decoder;
    private AtomicBoolean stop = new AtomicBoolean(true);

    public RpcNioSelector(SerializableEncoder encoder, SerializableDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
        try {
            selector = Selector.open();
            connectorCache = new ConcurrentHashMap<SocketChannel, RpcNioConnector>();
            connectors = new CopyOnWriteArrayList<RpcNioConnector>();
        } catch (IOException e) {
            throw Exceptions.runtime("", e);
        }
    }

    private void addSelectTask(Runnable task) {
        selectTasks.offer(task);
    }

    private boolean hasTask() {
        Runnable peek = selectTasks.peek();
        return peek != null;
    }

    private void runSelectTasks() {
        Runnable peek = selectTasks.peek();
        while (peek != null) {
            peek = selectTasks.pop();
            peek.run();
            peek = selectTasks.peek();
        }
    }

    @Override
    public void notifySend() {
        selector.wakeup();
    }

    //H.J TODO : 2021/2/24 这个为啥不用父类了
    @Override
    public void fireRpcResponse(RpcNioConnector connector, SwiftResponse object) {

    }

    @Override
    public void register(final RpcNioConnector connector) {
        this.addSelectTask(new Runnable() {
            @Override
            public void run() {
                try {
                    SelectionKey selectionKey = connector.getChannel().register(selector, READ_OP);
                    RpcNioSelector.this.initNewSocketChannel(connector.getChannel(), connector, selectionKey);
                } catch (Exception e) {
                    connector.handlerException(e);
                }
            }
        });
        this.notifySend();
    }

    private boolean doDispatchSelectionKey(SelectionKey selectionKey) {
        boolean result = false;
        try {
            if (selectionKey.isAcceptable()) {
                result = doAccept(selectionKey);
            }
            if (selectionKey.isReadable()) {
                result = doRead(selectionKey);
            }
            if (selectionKey.isWritable()) {
                result = doWrite(selectionKey);
            }
        } catch (Exception e) {
            handSelectionKeyException(selectionKey, e);
        }
        return result;
    }

    private void initNewSocketChannel(SocketChannel channel, RpcNioConnector connector, SelectionKey selectionKey) {
        connectorCache.put(channel, connector);
        connectors.add(connector);
    }

    @Override
    protected void setUpSelector() {
        if (!started.get()) {
            stop.set(false);
            started.set(true);
            thread.start();
        }
    }

    @Override
    protected void shutdownSelector() {
        stop.set(true);
        started.set(false);
    }

    private boolean doAccept(SelectionKey selectionKey) {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
        try {
            SocketChannel client = server.accept();
            if (client != null) {
                client.configureBlocking(false);
                RpcNioConnector connector = new RpcNioConnector(client, this);
                this.register(connector);
                connector.start();
                return true;
            }
        } catch (Exception e) {
            handSelectionKeyException(selectionKey, e);
        }
        return false;
    }

    private boolean doRead(SelectionKey selectionKey) {
        boolean result = false;
        SocketChannel client = (SocketChannel) selectionKey.channel();
        RpcNioConnector connector = connectorCache.get(client);
        if (connector != null) {
            try {
                while (!stop.get()) {
                    try {
                        Object object = decoder.decodeFromChannel(client);
                        if (object instanceof SwiftResponse) {
                            this.fireRpcResponse(connector, (SwiftResponse) object);
                        }
                        result = true;
                        break;
                    } catch (NoCodecResponseException e) {
                        break;
                    } catch (Exception e) {
                        handSelectionKeyException(selectionKey, e);
                        break;
                    }
                }
            } catch (Exception e) {
                handSelectionKeyException(selectionKey, e);
            }
        }
        return result;
    }

    private void handSelectionKeyException(final SelectionKey selectionKey, Exception e) {
        SelectableChannel channel = selectionKey.channel();
        RpcNioConnector connector = connectorCache.get(channel);
        if (connector != null) {
            this.fireRpcException(connector, e);
        }
    }

    private boolean doWrite(SelectionKey selectionKey) {
        boolean result = false;
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        RpcNioConnector connector = connectorCache.get(channel);
        if (connector.isNeedToSend()) {
            try {
                while (connector.isNeedToSend()) {
                    SwiftRequest rpc = connector.getRequest();
                    channel.write(encoder.encodeBuf(rpc));
                    result = true;
                }
                if (!connector.isNeedToSend()) {
                    selectionKey.interestOps(READ_OP);
                }
            } catch (Exception e) {
                handSelectionKeyException(selectionKey, e);
            }
        }
        return result;
    }

    private boolean checkSend() {
        boolean needSend = false;
        for (RpcNioConnector connector : connectors) {
            if (connector.isNeedToSend()) {
                SelectionKey selectionKey = connector.getChannel().keyFor(selector);
                selectionKey.interestOps(READ_WRITE_OP);
                needSend = true;
            }
        }
        return needSend;
    }

    private class SelectionThread extends Thread {

        public SelectionThread() {
            super("swift-nio-rpc-selection-thread");
        }

        @Override
        public void run() {
            while (!stop.get()) {
                if (RpcNioSelector.this.hasTask()) {
                    RpcNioSelector.this.runSelectTasks();
                }
                boolean needSend = checkSend();
                try {
                    if (needSend) {
                        selector.selectNow();
                    } else {
                        selector.select(10000);
                    }
                } catch (IOException e) {
//                    SwiftLoggers.getLogger().error(e);
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    doDispatchSelectionKey(selectionKey);
                }
            }
            try {
                selector.close();
            } catch (IOException e) {
//                SwiftLoggers.getLogger().error(e);
            }
        }

    }
}

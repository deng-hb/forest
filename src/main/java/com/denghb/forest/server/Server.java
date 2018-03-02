package com.denghb.forest.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 端口号优先运行指定参数
 */
public class Server {

    public static int DEFAULT_PORT = 8888;

    private boolean shutdown = false;

    public interface Handler {
        Response execute(Request request);
    }

    private Handler handler;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Server() {

    }

    public void start() {
        start(DEFAULT_PORT);
    }

    public void start(int port) {

        try {
            run(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    private void run(int port) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.setReuseAddress(true);

        serverSocket.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started http://localhost:" + port);

        while (!shutdown) {
            //查询就绪的通道数量
            int readyChannels = selector.select();
            //没有就绪的则继续进行循环
            if (readyChannels == 0)
                continue;
            //获得就绪的selectionKey的set集合
            Set<SelectionKey> keys = selector.selectedKeys();
            //获得set集合的迭代器
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = server.accept();
                    if (socketChannel != null) {
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                } else if (key.isReadable()) {
                    // 读
                    String message = null;
                    try {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        message = receive(socketChannel);
                        // 防止心跳包
                        if ("".equals(message)) {
                            continue;
                        }
                        // 客户端IP
                        String hostAddress = socketChannel.socket().getInetAddress().getHostAddress();

                        Response response = new Response();
                        if (null != handler) {
                            response = handler.execute(new Request(hostAddress, message));
                        }
                        byte[] bytes = response.bytes();

                        // 写
                        socketChannel.register(selector, SelectionKey.OP_WRITE);
                        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
                        buffer.put(bytes);
                        buffer.flip();
                        socketChannel.write(buffer);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (key.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.close();
                }
            }
        }

    }

    private String receive(SocketChannel socketChannel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        byte[] bytes = null;
        int size = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((size = socketChannel.read(buffer)) > 0) {
            buffer.flip();
            bytes = new byte[size];
            buffer.get(bytes);
            baos.write(bytes);
            buffer.clear();
        }
        bytes = baos.toByteArray();
        baos.close();
        return new String(bytes);
    }

}

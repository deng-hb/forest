package com.denghb.restful;

import com.denghb.json.JSON;
import com.denghb.utils.ConfigUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 端口号优先运行指定参数
 */
public class Server {

    private static int DEFAULT_PORT = 8888;

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
        start(null);
    }

    /**
     * -p=8080
     *
     * @param args
     */
    public void start(String[] args) {

        int port = DEFAULT_PORT;

        try {

            String port1 = ConfigUtils.getValue("port");
            if (null != port1) {
                port = Integer.parseInt(port1);
            }

            if (null != args) {
                for (String p : args) {
                    if (p.startsWith("-p")) {
                        p = p.substring(p.indexOf("=") + 1, p.length()).trim();
                        port = Integer.parseInt(p);
                    }
                }
            }

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
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = server.accept();
                    if (socketChannel != null) {
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                } else if (key.isReadable()) {
                    try {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        String message = receive(socketChannel);
                        // 防止心跳包
                        if ("".equals(message)) {
                            continue;
                        }

                        Response response = new Response();
                        if (null != handler) {
                            response = handler.execute(new Request(message));
                        }
                        byte[] bytes = response.bytes();
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
                iterator.remove();
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

    // 这真不是servlet
    public static class Request {

        private String method;

        private String uri;

        private Map<String, String> parameters = new HashMap<String, String>();

        private Map<String, String> headers = new HashMap<String, String>();

        /**
         * 解析报文，待优化
         *
         * @param message
         */
        public Request(String message) {

            // GET /xxx?a=aa HTTP/1.1
            int firstStart = message.indexOf(" ");
            this.method = message.substring(0, firstStart);
            String uri = message.substring(firstStart + 1, message.indexOf(" ", message.indexOf(" ") + 1));
            //有问号表示后面跟有参数
            int start = uri.indexOf("?");
            if (-1 != start) {
                String attr = uri.substring(start + 1, uri.length());
                uri = uri.substring(0, start);

                buildParameter(this.parameters, attr);
            }
            this.uri = uri;

            String headerStr = message.substring(message.indexOf("\r\n") + 2, message.indexOf("\r\n\r\n"));

            for (String header : headerStr.split("\r\n")) {
                String[] heads = header.split(": ");
                if (heads.length != 2) {
                    continue;
                }
                String key = heads[0];
                String value = heads[1];
                this.headers.put(key, value.trim());
            }

            // 请求参数
            String p = message.substring(message.indexOf("\r\n\r\n") + 4, message.length());
            buildParameter(this.parameters, p);
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "method='" + method + '\'' +
                    ", uri='" + uri + '\'' +
                    ", parameters=" + parameters +
                    ", headers=" + headers +
                    '}';
        }
    }

    static class Response {

        // 先默认都返回成功
        private static final String RESPONSE_HTML = "HTTP/1.1 %s\r\nContent-Type: %s\r\nConnection: close\r\n\r\n";

        private int code = 200;

        private String type = "application/json";

        private Object body;

        private Response() {

        }

        private Response(Object body) {
            this.body = body;
        }

        private Response(int code) {
            this.code = code;
        }

        /**
         * 响应字节
         *
         * @return
         */
        public byte[] bytes() {

            String header = "";
            byte[] bytes = new byte[0];

            if (body instanceof String) {
                bytes = String.valueOf(body).getBytes();
                type = "text/html";
            } else if (body instanceof File) {

                File file = (File) body;

                String fileName = file.getAbsolutePath().toLowerCase();
                if (fileName.endsWith("html")) {
                    type = "text/html";
                } else if (fileName.endsWith("jpg") || fileName.endsWith("jpeg")) {
                    type = "image/jpeg";
                } else if (fileName.endsWith("js")) {
                    type = "application/x-javascript";
                } else if (fileName.endsWith("png")) {
                    type = "image/png";
                } else if (fileName.endsWith("gif")) {
                    type = "image/gif";
                } else if (fileName.endsWith("css")) {
                    type = "text/css";
                }

                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] b = new byte[1024];

                    int n;

                    while ((n = fis.read(b)) != -1) {
                        bos.write(b, 0, n);
                    }

                    bytes = bos.toByteArray();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                bytes = JSON.toJSON(body).getBytes();
            }

            // TODO
            String status = "";
            switch (code) {
                case 200:
                    status = "200 OK";
                    break;
                case 403:
                    status = "403 Forbidden";
                    break;
                case 404:
                    status = "404 Not Found";
                    break;
                case 405:
                    status = "405 Method Not Allowed";
                    break;
                case 500:
                    status = "500 Internal Server Error";
                    break;
            }

            header = String.format(RESPONSE_HTML, status, type, body);

            return addBytes(header.getBytes(), bytes);
        }

        public static Response build(Object body) {
            return new Response(body);
        }

        public static Response buildError(int code) {
            return new Response(code);
        }

        public byte[] addBytes(byte[] data1, byte[] data2) {
            byte[] data3 = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, data3, 0, data1.length);
            System.arraycopy(data2, 0, data3, data1.length, data2.length);
            return data3;

        }

    }

    private static void buildParameter(Map<String, String> param, String p) {
        if ("".equals(p)) {
            return;
        }

        // JSON ?
        if (p.startsWith("{")) {
            Map a = JSON.parseJSON(Map.class, p);

            param.putAll(a);

            return;
        }

        String[] attrs = p.split("&");
        for (String string : attrs) {
            String key = string.substring(0, string.indexOf("="));
            String value = string.substring(string.indexOf("=") + 1);

            try {
                value = URLDecoder.decode(value, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            param.put(key, value);
        }
    }
}

package com.denghb.http;

import com.denghb.log.Log;
import com.denghb.log.LogFactory;
import com.denghb.utils.ThreadUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 端口号优先运行指定参数
 */
public class Server {

    private static Log log = LogFactory.getLog(Server.class);

    public static int DEFAULT_PORT = 8888;

    private boolean shutdown = false;

    private com.denghb.http.Handler handler;

    public com.denghb.http.Handler getHandler() {
        return handler;
    }

    public void setHandler(com.denghb.http.Handler handler) {
        this.handler = handler;
    }

    public Server() {

    }

    public void start(int port) {

        try {

            log.info("Server started http://localhost:" + port);
            ServerSocket serverSocket = new ServerSocket(port);
            while (!shutdown) {
                Socket socket = serverSocket.accept();
                ThreadUtils.submit(new Handler(socket));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    class Handler implements Runnable {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                service();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        private void service() throws Exception {

            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            try {
                input = new BufferedInputStream(socket.getInputStream());
                output = new BufferedOutputStream(socket.getOutputStream());

                StringBuilder message = new StringBuilder();
                int size = 1024;
                byte[] bytes = new byte[size];
                int tmp;
                while (true) {
                    try {
                        tmp = input.read(bytes);
                        message.append(new String(bytes, "ISO-8859-1"));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        tmp = -1;
                    }
                    if (tmp < size) {
                        break;
                    }
                }
                Request request = new Request(message.toString());
                request.setHostAddress(socket.getInetAddress().getHostAddress());

                Response response = null;
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    response = new Response();
                } else {
                    response = handler.execute(request);
                }
                output.write(response.bytes());
                output.flush();

            } finally {
                close(input);
                close(output);
                close(socket);
            }

        }
    }

    private void close(Closeable closeable) {

        try {
            closeable.close();
        } catch (Exception e) {

        }
    }
}

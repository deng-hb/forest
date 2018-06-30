package com.denghb.http;

import com.denghb.forest.Application;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 端口号优先运行指定参数
 */
public class Server {

    private static Log log = LogFactory.getLog(Application.class);

    public static int DEFAULT_PORT = 8888;

    private static ExecutorService service = Executors.newFixedThreadPool(20, new ThreadFactory() {
        AtomicInteger atomic = new AtomicInteger();

        public Thread newThread(Runnable r) {
            return new Thread(r, "server-" + this.atomic.getAndIncrement());
        }
    });

    private boolean shutdown = false;

    private ServerHandler handler;

    public ServerHandler getHandler() {
        return handler;
    }

    public void setHandler(ServerHandler handler) {
        this.handler = handler;
    }

    public Server() {

    }

    public void start(int port) {

        try {
            run(port);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    private void run(int port) throws IOException {

        log.info("Server started http://localhost:" + port);
        ServerSocket serverSocket = new ServerSocket(port);
        while (!shutdown) {
            Socket socket = serverSocket.accept();
            service.submit(new Handler(socket));
        }
    }

    class Handler implements Runnable {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                service();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        private void service() throws IOException {

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
                        message.append(new String(bytes, 0, tmp));
                    } catch (Exception e) {
                        tmp = -1;
                    }
                    if (tmp < size) {
                        break;
                    }
                }
                Request request = new Request(message.toString());
                Response response = handler.execute(request);
                request.getParameters().clear();
                request.getMultipartFileMap().clear();
                output.write(response.bytes());

            } finally {
                try {
                    output.close();
                } catch (Exception e) {

                }
            }

        }
    }
}

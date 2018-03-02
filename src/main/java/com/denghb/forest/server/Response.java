package com.denghb.forest.server;

import com.denghb.json.JSON;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class Response {

    // HTTP响应
    private static final String RESPONSE_HTML = "HTTP/1.1 %s\r\nServer: Forest/1.0\r\nContent-Type: %s\r\nConnection: close\r\n\r\n";

    private Map<String, String> headers = new HashMap<String, String>();

    private int code = 200;

    private String type = "application/json";

    private Object body;

    public Response() {

    }

    private Response(Object body) {
        this.body = body;
    }

    private Response(int code) {
        this.code = code;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * 响应字节
     *
     * @return
     */
    public byte[] bytes() {

        String header = "";
        byte[] bytes = new byte[0];

        try {
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
                fis = new FileInputStream(file);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                byte[] b = new byte[1024];

                int n;

                while ((n = fis.read(b)) != -1) {
                    bos.write(b, 0, n);
                }

                bytes = bos.toByteArray();
            } else {
                bytes = JSON.toJSON(body).getBytes();
            }
        } catch (Exception e) {
            e.printStackTrace();
            code = 500;
        }

        // TODO
        String status = "";
        switch (code) {
            case 200:
                status = "200 OK";
                break;
            case 301:
                status = "301 Moved Permanently";
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
                status = "500 Internal Server ErrorHandler";
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
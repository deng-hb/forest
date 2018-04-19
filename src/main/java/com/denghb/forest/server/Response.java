package com.denghb.forest.server;

import com.denghb.json.JSON;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Response {

    private static final Log log = LogFactory.getLog(Response.class);

    // HTTP响应
    private static final String RESPONSE_HTML = "HTTP/1.1 %s\r\nServer: Forest/1.0\r\nContent-Type: %s\r\nAccess-Control-Allow-Origin: *\r\nConnection: close\r\n\r\n";

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

        byte[] bytes = new byte[0];
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            if (body instanceof String) {
                bytes = String.valueOf(body).getBytes();
                type = "text/html;charset=UTF-8";
            } else if (body instanceof File) {

                File file = (File) body;

                String fileName = file.getAbsolutePath().toLowerCase();
                if (fileName.endsWith("html")) {
                    type = "text/html;charset=UTF-8";
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

                fis = new FileInputStream(file);
                bos = new ByteArrayOutputStream();

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
            log.error(e.getMessage(), e);
            code = 500;
        } finally {
            if (null != fis) {

                try {
                    fis.close();
                } catch (IOException e) {

                    log.error(e.getMessage(), e);
                }
            }
            if (null != bos) {

                try {
                    bos.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
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
                status = "500 Internal Server";
                break;
        }

        String resp = String.format(RESPONSE_HTML, status, type, body);

        return addBytes(resp.getBytes(), bytes);
    }

    public static Response build(Object body) {
        Response response = new Response(body);
        return response;
    }

    public static Response buildError(int code) {
        return new Response(code);
    }

    private byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;

    }

}
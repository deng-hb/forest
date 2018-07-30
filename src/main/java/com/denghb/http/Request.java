package com.denghb.http;

import com.denghb.json.JSON;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private String hostAddress;

    private String method;

    private String uri;

    private Map<String, String> parameters = new HashMap<String, String>();

    private Map<String, MultipartFile> multipartFileMap = new HashMap<String, MultipartFile>();

    private Map<String, String> headers = new HashMap<String, String>();

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

            buildParameter(attr);
        }
        this.uri = uri;

        int headersEnd = message.indexOf("\r\n\r\n");
        String headers = message.substring(message.indexOf("\r\n") + 2, headersEnd);

        for (String header : headers.split("\r\n")) {
            String[] ss = header.split(": ");
            if (ss.length != 2) {
                continue;
            }
            String key = ss[0];
            String value = ss[1];
            // 统一转小写
            this.headers.put(key.toLowerCase(), value.trim());
        }

        // 读取整个请求体
        String body = message.substring(headersEnd + 4);
        if (0 == body.length()) {
            return;
        }

        // 文件
        String contentType = this.headers.get("content-type");
        if (contentType.startsWith("multipart/form-data")) {
            buildMultipart(body);
        } else {
            // 字符读取
            buildParameter(body);
        }

    }

    private void buildMultipart(String body) {
        String boundary = body.substring(0, body.indexOf("\r\n"));
        body = body.substring(boundary.length() + 2, body.length());
        String gap = "\r\n" + boundary;

        int end;
        while (-1 != (end = body.indexOf(gap))) {
            String content = body.substring(0, end);

            // name
            int nameStart = content.indexOf("name=\"") + 6;
            content = content.substring(nameStart);
            int nameEnd = content.indexOf("\"");
            String name = content.substring(0, nameEnd);
            name = toUtf8(name);

            // file name
            String filename = null;
            int filenameStart = content.indexOf("filename=\"") + 10;
            if (10 < filenameStart) {
                content = content.substring(filenameStart);
                int filenameEnd = content.indexOf("\"");
                filename = content.substring(0, filenameEnd);
            }

            String contentType = null;
            int contentTypeStart = content.indexOf("Content-Type: ") + 14;
            if (14 < contentTypeStart) {
                content = content.substring(contentTypeStart);
                int contentTypeEnd = content.indexOf("\r\n");
                contentType = content.substring(0, contentTypeEnd);
            }

            // value
            int valueStart = content.indexOf("\r\n\r\n") + 4;
            String value = content.substring(valueStart);

            if (null != filename) {
                try {
                    byte[] bytes = value.getBytes("ISO-8859-1");

                    MultipartFile file = new MultipartFile();
                    file.setContentType(contentType);
                    file.setData(bytes);
                    file.setFilename(filename);

                    this.multipartFileMap.put(name, file);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                value = toUtf8(value);
                this.parameters.put(name, value);
            }

            body = body.substring(end + gap.length() + 2);
        }

    }

    private String toUtf8(String str) {
        try {
            return new String(str.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
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

    private void buildParameter(String p) {
        if (null == p || "".equals(p.trim())) {
            return;
        }

        p = toUtf8(p);
        // JSON ?
        if (p.startsWith("{")) {
            Map a = JSON.parseJSON(Map.class, p);
            this.parameters.putAll(a);
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
            this.parameters.put(key, value);
        }
    }

    public Map<String, MultipartFile> getMultipartFileMap() {
        return multipartFileMap;
    }

    public void setMultipartFileMap(Map<String, MultipartFile> multipartFileMap) {
        this.multipartFileMap = multipartFileMap;
    }
}
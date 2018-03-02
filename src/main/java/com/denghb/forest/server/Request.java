package com.denghb.forest.server;

import com.denghb.json.JSON;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private String hostAddress;

    private String method;

    private String uri;

    private Map<String, String> parameters = new HashMap<String, String>();

    private Map<String, String> headers = new HashMap<String, String>();

    /**
     * 解析报文，待优化
     *
     * @param message
     */
    public Request(String hostAddress, String message) {

        this.hostAddress = hostAddress;

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

    private void buildParameter(Map<String, String> param, String p) {
        if (null == p || "".equals(p.trim())) {
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
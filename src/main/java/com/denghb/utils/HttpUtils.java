package com.denghb.utils;

import com.denghb.log.Log;
import com.denghb.log.LogFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by denghb on 16/8/29.
 */
public class HttpUtils {

    private static final Log log = LogFactory.getLog(HttpUtils.class);

    /**
     * 获取数据
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        HttpURLConnection connection = null;
        try {
            connection = getHttpConnection(url);
            connection.setRequestMethod("GET");//设置请求的方式

            // 返回信息

            InputStream in = connection.getInputStream();
            if (null == in) {
                in = connection.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = br.readLine()) != null) {//BufferedReader特有功能，一次读取一行数据
                buffer.append(str);
            }
            // System.out.println(buffer);
            in.close();
            br.close();

            String response = buffer.toString();
            log.debug("url:{}\nresponse:\n{}", response);
            return response;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            connection.disconnect();//使用完关闭TCP连接，释放资源
        }
        return null;
    }

    /**
     * 提交数据
     *
     * @param url
     * @param body
     * @return
     */
    public static String post(String url, String body) {
        HttpURLConnection connection = null;
        try {
            connection = getHttpConnection(url);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");
            
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(body);
            bw.flush();
            out.close();
            bw.close();//使用完关闭

            // 返回信息

            InputStream in = connection.getInputStream();
            if (null == in) {
                in = connection.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = br.readLine()) != null) {//BufferedReader特有功能，一次读取一行数据
                buffer.append(str);
            }
            in.close();
            br.close();

            String response = buffer.toString();
            log.debug("url:{}\nresponse:\n{}", response);
            return response;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            connection.disconnect();//使用完关闭TCP连接，释放资源
        }
        return null;
    }
    /**
     * @param url
     * @param param
     * @return body
     */
    public static String post(String url, Map<String, String> param) {
        String resp = null;
        HttpURLConnection connection = null;
        try {
            connection = getHttpConnection(url);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            bw.write(map2String(param));
            bw.flush();
            out.close();
            bw.close();//使用完关闭

            // 返回信息

            InputStream in = connection.getInputStream();
            if (null == in) {
                in = connection.getErrorStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String str = null;
            StringBuffer buffer = new StringBuffer();
            while ((str = br.readLine()) != null) {//BufferedReader特有功能，一次读取一行数据
                buffer.append(str);
            }
            // System.out.println(buffer);
            in.close();
            br.close();

            resp = buffer.toString();

        } catch (Exception e) {
            log.error(url + "\n" + e.getMessage(), e);
        } finally {
            connection.disconnect();//使用完关闭TCP连接，释放资源
        }
        return resp;
    }

    private static String map2String(Map<String, String> param) {
        StringBuffer sb = new StringBuffer();
        for (String key : param.keySet()) {
            sb.append(key);
            sb.append('=');
            sb.append(param.get(key));
            sb.append('&');
        }
        return sb.toString();
    }

    private static HttpURLConnection getHttpConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(3000);// 3秒
        connection.setReadTimeout(60000);// 1分钟
        connection.setUseCaches(false);
        connection.setDoInput(true);//设置这个连接是否可以写入数据
        connection.setDoOutput(true);//设置这个连接是否可以输出数据
        // 请求头
        connection.setRequestProperty("User-Agent", "com.denghb.httputils");
//        connection.setRequestProperty("User-Agent", "itunesstored/1.0 iOS/11.0.3 model/iPhone9,1 hwp/t8010 build/15A432 (6; dt:137)");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-type", "application/json; charset=UTF-8");
        connection.setAllowUserInteraction(false);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Proxy-Connection", "Keep-Alive");

        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustAllSSLSocketFactory());
        }
        return connection;
    }

    private static SSLSocketFactory getTrustAllSSLSocketFactory() {
        // 信任所有证书
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, null);
            return sslContext.getSocketFactory();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

}
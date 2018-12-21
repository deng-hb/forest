package com.denghb.forest;


import com.denghb.http.Server;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;
import com.denghb.utils.ConfigUtils;


/**
 * @author denghb
 */
public class Application {

    private static Log log = LogFactory.getLog(Application.class);

    static Server _SERVER = new Server();

    private Application() {

    }

    public static void run(Class clazz, String[] args) {

        long start = System.currentTimeMillis();


        String configPath = null;
        int port = Server.DEFAULT_PORT;
        if (null != args) {
            for (String p : args) {
                if (p.startsWith("-p")) {
                    p = p.substring(p.indexOf("=") + 1, p.length()).trim();
                    port = Integer.parseInt(p);
                }
                if (p.startsWith("-config")) {
                    configPath = p.substring(p.indexOf("=") + 1, p.length()).trim();
                }
            }
        }
        ConfigUtils.init(configPath);


        final boolean debug = "true".equals(ConfigUtils.getValue("debug"));

        if (debug) {
            log.info("Config debug starting ...");
        } else {
            log.info("Config starting ...");
        }

        Config.init(clazz);

        String port1 = ConfigUtils.getValue("port");
        if (null != port1) {
            port = Integer.parseInt(port1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                stop();
            }
        }));

        log.info("Config started ⚡ " + (System.currentTimeMillis() - start) / 1000.0 + "s");


        // 在start之前
        _SERVER.setHandler(new RESTfulHandler(log, debug));
        _SERVER.start(port);

    }

    /**
     * 停止服务
     */
    public static void stop() {
        log.info("Config shutdown");
        if (null != _SERVER) {
            _SERVER.shutdown();
            _SERVER = null;
        }
    }


}

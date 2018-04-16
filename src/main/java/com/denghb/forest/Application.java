package com.denghb.forest;


import com.denghb.forest.server.Server;
import com.denghb.forest.utils.ClassUtils;
import com.denghb.log.Log;
import com.denghb.utils.ConfigUtils;


/**
 * @author denghb
 */
public class Application {

    private static Log log = ClassUtils.create(Log.class, Application.class);

    static Server _SERVER = new Server();

    private Application() {

    }

    public static void run(Class clazz, String[] args) {

        long start = System.currentTimeMillis();

        final boolean debug = "true".equals(ConfigUtils.getValue("debug"));

        if (debug) {
            System.out.println("Forest debug starting ...");
        } else {

            System.out.println("Forest starting ...");
        }

        Forest.init(clazz);

        // 在start之前
        _SERVER.setHandler(new ForestHandler(log, debug));


        int port = Server.DEFAULT_PORT;
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
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                stop();
            }
        }));

        System.out.println("Forest started ⚡ " + (System.currentTimeMillis() - start) / 1000.0 + "s");

        _SERVER.start(port);

    }

    /**
     * 停止服务
     */
    public static void stop() {
        System.out.println("Forest shutdown");
        if (null != _SERVER) {
            _SERVER.shutdown();
            _SERVER = null;
        }
    }


}

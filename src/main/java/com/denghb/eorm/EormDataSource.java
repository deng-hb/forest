package com.denghb.eorm;

import com.denghb.eorm.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EormDataSource {

    private static ThreadLocal<Connection> tx = new ThreadLocal<Connection>();

    private static Connection connection;

    public static class Config {

        public static String url;

        public static String username;

        public static String password;

    }
    private EormDataSource() {

    }

    private static Connection createConnection() {
        try {
            return DriverManager.getConnection(Config.url, Config.username, Config.password);
        } catch (Exception e) {
            throw new EormRuntimeException(e);
        }
    }

    private static Connection connection() {

        if (null == connection) {
            connection = createConnection();
        }
        return connection;
    }

    public static Connection getConnection() {
        Connection conn = tx.get();
        if (null != conn) {
            return conn;
        }
        return connection();
    }

    public static void begin() {
        try {
            Connection conn = createConnection();
            conn.setAutoCommit(false);
            tx.set(conn);
        } catch (SQLException e) {
            throw new EormRuntimeException(e);
        }
    }

    public static void commit() {
        try {
            Connection conn = tx.get();
            conn.commit();
            tx.remove();
            JdbcUtils.close(conn);
        } catch (Exception e) {
            throw new EormRuntimeException(e);
        }
    }

    public static void rollback() {
        try {
            Connection conn = tx.get();
            conn.rollback();
            tx.remove();
            JdbcUtils.close(conn);
        } catch (Exception e) {
            throw new EormRuntimeException(e);
        }
    }
}

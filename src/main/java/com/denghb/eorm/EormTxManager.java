package com.denghb.eorm;

import com.denghb.eorm.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class EormTxManager {

    private static ThreadLocal<Connection> txConnection = new ThreadLocal<Connection>();

    private static Connection connection;

    public static String url;

    public static String username;

    public static String password;

    private EormTxManager(){

    }

    public static Connection createConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("connection error");
        }
    }

    public static Connection getConnection() {

        if (null == connection) {
            connection = createConnection();
        }
        return connection;
    }
    public static Connection getTxConnection() {
        Connection tx = txConnection.get();
        if (null != tx) {
            return tx;
        }
        return getConnection();
    }

    public static void begin() throws SQLException {
        Connection conn = createConnection();
        conn.setAutoCommit(false);
        txConnection.set(conn);
    }

    public static void commit() throws SQLException {
        Connection tx = txConnection.get();
        tx.commit();
        txConnection.remove();
        JdbcUtils.close(tx);
    }

    public static void rollback() throws SQLException {
        Connection tx = txConnection.get();
        tx.rollback();
        txConnection.remove();
        JdbcUtils.close(tx);
    }
}

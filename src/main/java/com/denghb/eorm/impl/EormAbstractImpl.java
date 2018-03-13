package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.utils.EormUtils;
import com.denghb.eorm.utils.JdbcUtils;
import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象实现
 * insert
 * batchInsert
 * list
 * doTx
 */
public abstract class EormAbstractImpl implements Eorm {

    private Connection connection;

    private String url;

    private String username;

    private String password;

    public EormAbstractImpl(Connection connection) {
        this.connection = connection;
    }

    public EormAbstractImpl(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.connection = createConnection();
    }

    public Connection createConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("connection error");
        }
    }

    public int execute(String sql, Object... args) {
        int res = 0;
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);

            int i = 1;
            for (Object object : args) {
                ps.setObject(i, object);
                i++;
            }
            res = ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.close(ps);
        }
        return res;
    }

    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {
        List<T> list = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);

            int i = 1;
            for (Object object : args) {
                ps.setObject(i, object);
                i++;
            }

            rs = ps.executeQuery();
            ResultSetMetaData data = rs.getMetaData();

            // map
            if (Map.class.isAssignableFrom(clazz)) {
                list = new ArrayList<T>();

                int columnCount = data.getColumnCount();

                while (rs.next()) {

                    Map<String, Object> map = new HashMap<String, Object>();
                    for (int j = 1; j <= columnCount; j++) {
                        String columnName = data.getColumnName(j);
                        map.put(columnName, rs.getObject(columnName));
                    }

                    list.add((T) map);
                }
            } else if (CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)) {

                // 单个类型 Integer、String、Long、Double
                list = new ArrayList<T>();
                while (rs.next()) {
                    String columnName = data.getColumnName(1);
                    Object value = rs.getObject(columnName);

                    list.add((T) value);
                }
            } else {

                list = new ArrayList<T>();

                // 所有的字段
                List<Field> fields = ReflectUtils.getFields(clazz);

                int columnCount = data.getColumnCount();
                while (rs.next()) {
                    Object object = ReflectUtils.constructorInstance(clazz);

                    list.add((T) object);
                    for (int j = 1; j <= columnCount; j++) {

                        String columnName = data.getColumnLabel(j);
                        Object value = rs.getObject(columnName);
                        if (null == value) {
                            continue;
                        }

                        // 找到对应字段
                        for (Field field : fields) {
                            // 比较表字段名
                            Ecolumn ecolumn = field.getAnnotation(Ecolumn.class);
                            if (null != ecolumn) {
                                if (ecolumn.name().equalsIgnoreCase(columnName)) {
                                    ReflectUtils.setFieldValue(field, object, value);
                                    continue;
                                }
                            }
                            // 比较字段名
                            columnName = columnName.replace("_", "");
                            if (field.getName().equalsIgnoreCase(columnName)) {
                                ReflectUtils.setFieldValue(field, object, value);
                            }
                        }

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JdbcUtils.close(ps);
            JdbcUtils.close(rs);
        }
        return list;
    }

    public <T> int update(T domain) {

        EormUtils.TableInfo table = EormUtils.getTableInfo(domain);
        List<Object> params = new ArrayList<Object>();

        List<EormUtils.Column> commonColumns = table.getCommonColumns();
        StringBuilder ssb = new StringBuilder();
        for (int i = 0; i < commonColumns.size(); i++) {
            EormUtils.Column column = commonColumns.get(i);

            if (i > 0) {
                ssb.append(", ");
            }

            ssb.append("`");
            ssb.append(column.getName());
            ssb.append("` = ?");

            params.add(column.getValue());
        }

        List<EormUtils.Column> primaryKeyColumns = table.getPrimaryKeyColumns();
        StringBuilder wsb = new StringBuilder();
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            EormUtils.Column column = primaryKeyColumns.get(i);
            if (i > 0) {
                wsb.append(" and ");
            }
            wsb.append("`");
            wsb.append(column.getName());
            wsb.append("` = ?");

            params.add(column.getValue());
        }

        StringBuilder sb = new StringBuilder("update ");
        sb.append(table.getTableName());
        sb.append(" set ");
        sb.append(ssb);
        sb.append(" where ");
        sb.append(wsb);

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());
        return res;
    }

    public <T> int delete(T domain) {
        EormUtils.TableInfo table = EormUtils.getTableInfo(domain);
        List<Object> params = new ArrayList<Object>();

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(table.getTableName());
        sb.append(" where ");

        List<EormUtils.Column> primaryKeyColumns = table.getPrimaryKeyColumns();
        for (int i = 0; i < primaryKeyColumns.size(); i++) {
            EormUtils.Column column = primaryKeyColumns.get(i);
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append("`");
            sb.append(column.getName());
            sb.append("` = ?");

            params.add(column.getValue());
        }

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());
        return res;
    }

    public <T> int delete(Class<T> clazz, Object... ids) {

        StringBuilder sb = new StringBuilder("delete from ");
        sb.append(EormUtils.getTableName(clazz));
        sb.append(" where ");

        List<String> primaryKeyNames = EormUtils.getPrimaryKeyNames(clazz);
        for (int i = 0; i < primaryKeyNames.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append("`");
            sb.append(primaryKeyNames.get(i));
            sb.append("` = ?");
        }

        String sql = sb.toString();
        int res = this.execute(sql, ids);
        return res;
    }

    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {

        List<T> list = select(clazz, sql, args);
        if (null != list && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public <T> T selectByPrimaryKey(Class<T> clazz, Object... args) {

        // 表名
        String tableName = EormUtils.getTableName(clazz);
        StringBuilder sb = new StringBuilder("select * from ");
        sb.append(tableName);
        sb.append(" where ");

        // 主键名
        List<String> primaryKeyNames = EormUtils.getPrimaryKeyNames(clazz);
        for (int i = 0; i < primaryKeyNames.size(); i++) {
            if (i > 0) {
                sb.append(" and ");
            }
            sb.append("`");
            sb.append(primaryKeyNames.get(i));
            sb.append("` = ?");
        }

        return selectOne(clazz, sb.toString(), args);
    }

}

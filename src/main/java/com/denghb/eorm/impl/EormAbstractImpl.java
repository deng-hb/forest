package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormTxManager;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.utils.EormUtils;
import com.denghb.eorm.utils.JdbcUtils;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;
import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

/**
 * 抽象实现
 * insert
 * batchInsert
 * list
 * doTx
 */
public abstract class EormAbstractImpl implements Eorm {

    private static Log log = LogFactory.getLog(EormAbstractImpl.class);

    public int execute(String sql, Object... args) {

        int res = 0;
        PreparedStatement ps = null;
        try {
            ps = EormTxManager.getTxConnection().prepareStatement(sql);

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

        log.debug("execute:\n{}\t{}\n{}\n", res, args, sql);
        return res;
    }

    public <T> List<T> select(Class<T> clazz, String sql, Object... args) {

        List<T> list = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = EormTxManager.getTxConnection().prepareStatement(sql);

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
                    String columnName = data.getColumnLabel(1);
                    Object value = rs.getObject(columnName);

                    value = ReflectUtils.constructorInstance(clazz, String.class, String.valueOf(value));
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
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.close(ps);
            JdbcUtils.close(rs);
        }
        log.debug("select:\nList<{}>({})\t{}\n{}\n", clazz.getSimpleName(), list.size(), args, sql);
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

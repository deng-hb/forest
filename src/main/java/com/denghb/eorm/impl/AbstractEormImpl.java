package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.EormDataSource;
import com.denghb.eorm.EormRuntimeException;
import com.denghb.eorm.annotation.Ecolumn;
import com.denghb.eorm.utils.JdbcUtils;
import com.denghb.log.Log;
import com.denghb.log.LogFactory;
import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public abstract class AbstractEormImpl implements Eorm {

    Log log = LogFactory.getLog(this.getClass());

    public int execute(String sql, Object... args) {

        int res = 0;
        PreparedStatement ps = null;
        try {
            ps = EormDataSource.getConnection().prepareStatement(sql);

            int i = 1;
            for (Object object : args) {
                ps.setObject(i, object);
                i++;
            }
            res = ps.executeUpdate();
        } catch (Exception e) {
            throw new EormRuntimeException(e);
        } finally {
            JdbcUtils.close(ps);
        }
        if (log.isDebugEnabled()) {
            log.debug("execute:\n{}\t{}\n{}\n", res, args, sql);
        }
        return res;
    }

    public List<Map<String, Object>> selectMap(String sql, Object... args) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = EormDataSource.getConnection().prepareStatement(sql);

            int i = 1;
            for (Object object : args) {
                ps.setObject(i, object);
                i++;
            }

            rs = ps.executeQuery();
            ResultSetMetaData data = rs.getMetaData();

            int columnCount = data.getColumnCount();

            while (rs.next()) {

                Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 1; j <= columnCount; j++) {
                    String columnName = data.getColumnName(j);
                    map.put(columnName, rs.getObject(columnName));
                }

                list.add(map);
            }
        } catch (Exception e) {
            throw new EormRuntimeException(e);
        } finally {
            JdbcUtils.close(ps);
            JdbcUtils.close(rs);
        }
        if (log.isDebugEnabled()) {
            log.debug("select:\n({})\t{}\n{}\n", list.size(), args, sql);
        }
        return list;
    }

    public <T> List<T> selectList(Class<T> clazz, String sql, Object... args) {

        List<T> list = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = EormDataSource.getConnection().prepareStatement(sql);

            int i = 1;
            for (Object object : args) {
                ps.setObject(i, object);
                i++;
            }

            rs = ps.executeQuery();
            ResultSetMetaData data = rs.getMetaData();

            if (CharSequence.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)) {

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
}

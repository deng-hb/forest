package com.denghb.eorm.impl;

import com.denghb.eorm.Eorm;
import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.eorm.utils.EormUtils;
import com.denghb.eorm.utils.JdbcUtils;
import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite实现
 */
public class EormSQLiteImpl extends EormAbstractImpl implements Eorm {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public EormSQLiteImpl(Connection connection) {
        super(connection);
    }

    public EormSQLiteImpl(String url, String username, String password) {
        super(url, username, password);
    }

    public <T> int insert(T domain) {

        EormUtils.TableInfo table = EormUtils.getTableInfo(domain);

        List<Object> params = new ArrayList<Object>();

        StringBuilder csb = new StringBuilder();
        StringBuilder vsb = new StringBuilder();

        List<EormUtils.Column> columns = table.getAllColumns();
        for (int i = 0; i < columns.size(); i++) {

            EormUtils.Column column = columns.get(i);

            if (i > 0) {
                csb.append(", ");
                vsb.append(", ");
            }
            csb.append('`');
            csb.append(column.getName());
            csb.append('`');

            vsb.append("?");

            params.add(column.getValue());
        }

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table.getTableName());
        sb.append(" (");
        sb.append(csb);
        sb.append(") values (");
        sb.append(vsb);
        sb.append(")");

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());

        if (1 == res) {
            // 获取自动生成的ID并填充
            List<Field> fields = table.getAllPrimaryKeyFields();
            if (fields.size() == 1) {// 只适合单个主键
                Field field = fields.get(0);
                Object object = ReflectUtils.getFieldValue(field, domain);
                if (null == object) {
                    Object value = selectOne(field.getType(), "select last_insert_rowid() as id");
                    ReflectUtils.setFieldValue(field, domain, value);
                }
            }
        }

        return res;

    }

    public <T> int batchInsert(List<T> list) {
        int res = 0;
        for (T t : list) {
            this.insert(t);
            res++;
        }
        return res;
    }

    public <T> PagingResult<T> page(Class<T> clazz, StringBuffer sql, Paging paging) {
        PagingResult<T> result = new PagingResult<T>(paging);

        Object[] objects = paging.getParams().toArray();
        // 不分页 start
        long rows = paging.getRows();
        if (0 != rows) {
            // 先查总数
            String totalSql = "select count(*) ";

            String tempSql = sql.toString().toLowerCase();
            totalSql += sql.substring(tempSql.indexOf("from"), sql.length());

            // fix group by
            if (0 < totalSql.indexOf(" group ")) {
                totalSql = "select count(*) from (" + totalSql + ") temp";
            }

            long total = this.selectOne(Long.class, totalSql, objects);

            paging.setTotal(total);
            if (0 == total) {
                return result;
            }
        }
        // 不分页 end

        // 排序
        if (paging.isSort()) {
            // 判断是否有排序字段
            String[] sorts = paging.getSorts();
            if (null != sorts && 0 < sorts.length) {
                int sortIndex = paging.getSortIndex();

                // 大于排序的长度默认最后一个
                if (sortIndex >= sorts.length) {
                    sortIndex = sorts.length - 1;
                }
                sql.append(" order by ");

                sql.append('`');
                sql.append(sorts[sortIndex]);
                sql.append('`');

                // 排序方式
                if (paging.isDesc()) {
                    sql.append(" desc");
                } else {
                    sql.append(" asc");
                }
            }
        }

        if (0 != rows) {
            // 分页
            sql.append(" limit ");
            sql.append(paging.getStart());
            sql.append(",");
            sql.append(rows);
        }

        List<T> list = select(clazz, sql.toString(), objects);
        result.setList(list);

        return result;
    }

    public void doTx(Handler handler) {
        Connection conn = createConnection();
        try {
            conn.setAutoCommit(false);
            handler.doTx(new EormSQLiteImpl(conn));
            conn.commit();
        } catch (Exception e) {
            try {
                if (null != conn) {
                    conn.rollback();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            JdbcUtils.close(conn);
        }

    }
}
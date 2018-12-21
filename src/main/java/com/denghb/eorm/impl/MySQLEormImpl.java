package com.denghb.eorm.impl;

import com.denghb.eorm.EormRuntimeException;
import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;
import com.denghb.eorm.utils.EormUtils;
import com.denghb.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * MySQL 实现
 */
public class MySQLEormImpl extends AbstractEormImpl {

    static {

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            throw new EormRuntimeException(e.getMessage(), e);
        }
    }

    public <T> void update(T domain) {

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
        if (1 != res) {
            throw new EormRuntimeException();
        }
    }

    public <T> void delete(T domain) {
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

        if (1 != res) {
            throw new EormRuntimeException();
        }
    }

    public <T> void delete(Class<T> clazz, Object... ids) {

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

        if (1 != res) {
            throw new EormRuntimeException();
        }
    }

    public <T> T selectOne(Class<T> clazz, String sql, Object... args) {

        List<T> list = selectList(clazz, sql, args);
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

    public <T> void insert(T domain) {

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
                    Object value = selectOne(field.getType(), "select LAST_INSERT_ID() as id");
                    ReflectUtils.setFieldValue(field, domain, value);
                }
            }
        }

        if (1 != res) {
            throw new EormRuntimeException();
        }

    }

    public <T> void insert(List<T> list) {
        if (null == list || list.isEmpty()) {
            throw new EormRuntimeException();
        }

        // 取第一个样本
        T type = list.get(0);
        EormUtils.TableInfo table = EormUtils.getTableInfo(type);

        StringBuilder csb = new StringBuilder();
        List<EormUtils.Column> allColumns = table.getAllColumns();
        int columnSize = allColumns.size();
        for (int i = 0; i < columnSize; i++) {
            EormUtils.Column column = allColumns.get(i);
            if (i > 0) {
                csb.append(", ");
            }
            csb.append('`');
            csb.append(column.getName());
            csb.append('`');

        }

        List<Object> params = new ArrayList<Object>();

        StringBuilder vsb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                vsb.append(", ");
            }

            vsb.append("(");

            table = EormUtils.getTableInfo(list.get(i));

            if (columnSize != table.getAllColumns().size()) {
                throw new RuntimeException("column size difference ...");
            }

            for (int j = 0; j < columnSize; j++) {
                if (j > 0) {
                    vsb.append(", ");
                }
                vsb.append("?");

                params.add(table.getAllColumns().get(j).getValue());
            }

            vsb.append(")");

        }

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table.getTableName());
        sb.append(" (");
        sb.append(csb);

        sb.append(") values ");
        sb.append(vsb);

        String sql = sb.toString();
        int res = this.execute(sql, params.toArray());
        if (res != list.size()) {
            throw new EormRuntimeException();
        }
    }

    public <T> PagingResult<T> selectPage(Class<T> clazz, StringBuffer sql, Paging paging) {
        PagingResult<T> result = new PagingResult<T>(paging);

        Object[] objects = paging.getParams().toArray();
        // 不分页 start
        long pageSize = paging.getPageSize();
        if (0 != pageSize) {
            // 先查总数
            String totalSql;

            // fix group by
            if (paging.isFullPage()) {
                totalSql = "select count(*) from (" + sql + ") temp";
            } else {
                totalSql = "select count(*) ";
                String tempSql = sql.toString().toLowerCase();
                totalSql += sql.substring(tempSql.indexOf("from"), sql.length());
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

        if (0 != pageSize) {
            // 分页
            sql.append(" limit ");
            sql.append(paging.getStart());
            sql.append(",");
            sql.append(pageSize);
        }

        List<T> list = selectList(clazz, sql.toString(), objects);
        result.setList(list);

        return result;
    }
}

package com.denghb.eorm;

import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;

import java.util.List;

/**
 * Easy ROM
 *
 * @author denghb
 */
public interface Eorm {

    /**
     * 执行一条SQL
     *
     * @param sql
     * @param args
     * @return
     */
    public int execute(String sql, Object... args);

    /**
     * 执行一条查询
     *
     * @param clazz
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T> List<T> select(Class<T> clazz, String sql, Object... args);

    /**
     * 插入一个对象
     *
     * @param domain
     * @param <T>
     * @return
     */
    public <T> int insert(T domain);

    /**
     * 修改一个对象
     *
     * @param domain
     * @param <T>
     * @return
     */
    public <T> int update(T domain);

    /**
     * 删除一个对象
     *
     * @param domain
     * @param <T>
     * @return
     */
    public <T> int delete(T domain);


    /**
     * 删除多个主键的对象
     *
     * @param clazz
     * @param ids
     * @param <T>
     * @return
     */
    public <T> int delete(Class<T> clazz, Object... ids);

    /**
     * 查询一个对象
     *
     * @param clazz
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T> T selectOne(Class<T> clazz, String sql, Object... args);

    /**
     * 批量插入
     *
     * @param list
     * @param <T>
     * @return
     */
    public <T> int batchInsert(List<T> list);

    /**
     * 分页查询
     *
     * @param clazz
     * @param sql
     * @param paging
     * @param <T>
     * @return
     */
    public <T> PagingResult<T> list(Class<T> clazz, StringBuffer sql, Paging paging);

    public interface Handler {

        public void doTx(Eorm eorm);
    }

    /**
     * 事务处理
     *
     * @param handler
     */
    public void doTx(Handler handler);
}

package com.denghb.eorm;

import com.denghb.eorm.domain.Paging;
import com.denghb.eorm.domain.PagingResult;

import java.util.List;
import java.util.Map;

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
     * 插入一个对象
     *
     * @param domain
     * @param <T>
     * @return
     */
    public <T> void insert(T domain);


    /**
     * 批量插入
     *
     * @param list
     * @param <T>
     * @return
     */
    public <T> void insert(List<T> list);

    /**
     * 修改一个对象
     *
     * @param domain
     * @param <T>
     * @return
     */
    public <T> void update(T domain);

    /**
     * 删除一个对象
     *
     * @param domain
     * @param <T>
     * @return
     */
    public <T> void delete(T domain);


    /**
     * 删除多个主键的对象
     *
     * @param clazz
     * @param ids
     * @param <T>
     * @return
     */
    public <T> void delete(Class<T> clazz, Object... ids);

    /**
     * 执行一条查询
     *
     * @param sql
     * @param args
     * @return
     */
    public List<Map<String, Object>> selectMap(String sql, Object... args);

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
     * 按主键查询一条记录
     *
     * @param clazz
     * @param args
     * @param <T>
     * @return
     */
    public <T> T selectByPrimaryKey(Class<T> clazz, Object... args);

    /**
     * 执行一条查询
     *
     * @param clazz
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    public <T> List<T> selectList(Class<T> clazz, String sql, Object... args);

    /**
     * 分页查询
     *
     * @param clazz
     * @param sql
     * @param paging
     * @param <T>
     * @return
     */
    public <T> PagingResult<T> selectPage(Class<T> clazz, StringBuffer sql, Paging paging);


}

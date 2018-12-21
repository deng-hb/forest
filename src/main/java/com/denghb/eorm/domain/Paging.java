package com.denghb.eorm.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by denghb on 15/11/18.
 */
public abstract class Paging implements Serializable {

    private static final long serialVersionUID = 1760699317506200256L;

    /**
     * 总数
     */
    private long total = 0;

    /**
     * 当前页数
     */
    private long page = 1;

    /**
     * 每页数量
     */
    private long pageSize = 20;

    /**
     * 总页数
     */
    private long totalPage = 1;

    /**
     * 开始
     */
    private long start;

    /**
     * 整条SQL 统计数量
     */
    private boolean fullPage = false;

    /**
     * 是否排序
     */
    private boolean isSort = true;

    /**
     * 排序字段
     */
    private String[] sorts;

    /**
     * 排序字段下标 默认：0
     */
    private int sortIndex = 0;

    /**
     * 默认降序
     */
    private boolean desc = true;

    /**
     * 参数列表
     */
    private List<Object> params = new ArrayList<Object>();

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;

        if (0 != total && 0 != pageSize) {
            totalPage = total / pageSize;
            if (total % pageSize != 0) {
                totalPage++;
            }
        }

    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * 设置需要排序的数据库字段
     */
    public abstract String[] getSorts();

    public void setSorts(String[] sorts) {
        this.sorts = sorts;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public boolean isSort() {
        return isSort;
    }

    public void setSort(boolean isSort) {
        this.isSort = isSort;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public long getStart() {
        return (page - 1) * pageSize;
    }

    public void setStart(long start) {
        this.start = start;
    }


    public boolean isFullPage() {
        return fullPage;
    }

    public void setFullPage(boolean fullPage) {
        this.fullPage = fullPage;
    }

    @Override
    public String toString() {
        return "Paging{" +
                "total=" + total +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalPage=" + totalPage +
                ", start=" + start +
                ", fullPage=" + fullPage +
                ", isSort=" + isSort +
                ", sorts=" + Arrays.toString(sorts) +
                ", sortIndex=" + sortIndex +
                ", desc=" + desc +
                ", params=" + params +
                '}';
    }
}

package com.denghb.test.forest;

import java.util.Date;

/**

 CREATE TABLE `user` (
 `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
 `age` int(11) DEFAULT 18 COMMENT '年龄',
 `name` varchar(20) DEFAULT NULL COMMENT '姓名',
 `email` varchar(100) DEFAULT NULL COMMENT '邮件',
 `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
 `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
 `updated_time` datetime DEFAULT NULL,
 PRIMARY KEY (`id`)
 ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

 */
public class User {

    private Long id;

    private String name;

    private String mobile;

    private Date createdTime;

    private Date updatedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}

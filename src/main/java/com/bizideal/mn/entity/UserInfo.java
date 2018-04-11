package com.bizideal.mn.entity;

import java.io.Serializable;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 13:45
 * @version: 1.0
 * @Description:
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 963061236984604666L;
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public UserInfo setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserInfo setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":")
                .append(id);
        sb.append(",\"name\":\"")
                .append(name).append('\"');
        sb.append('}');
        return sb.toString();
    }
}

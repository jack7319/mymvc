package com.bizideal.mn.service;

import com.bizideal.mn.entity.UserInfo;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 15:10
 * @version: 1.0
 * @Description:
 */
public interface UserInfoService {

    int addOrUpdateUser(UserInfo userInfo);

    UserInfo getUser(int id);
}
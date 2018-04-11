package com.bizideal.mn.service.impl;

import com.bizideal.mn.annotation.MyService;
import com.bizideal.mn.entity.UserInfo;
import com.bizideal.mn.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 15:11
 * @version: 1.0
 * @Description:
 */
@MyService("servicelll")
public class UserInfoServiceImpl implements UserInfoService {

    private static Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    private Map<Integer, UserInfo> db = new HashMap<>();

    @Override
    public int addOrUpdateUser(UserInfo userInfo) {
        logger.debug("add user success..");
        int id = userInfo.getId();
        UserInfo u = db.get(id);
        if (null == u) {
            db.put(id, userInfo);
        } else {
            u.setName(userInfo.getName());
        }
        return 1;
    }

    @Override
    public UserInfo getUser(int id) {
        logger.debug("get user success..");
        return db.get(id);
    }
}

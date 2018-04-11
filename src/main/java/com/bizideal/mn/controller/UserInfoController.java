package com.bizideal.mn.controller;

import com.bizideal.mn.annotation.MyAutowired;
import com.bizideal.mn.annotation.MyController;
import com.bizideal.mn.annotation.MyRequestMapping;
import com.bizideal.mn.annotation.MyRequestParam;
import com.bizideal.mn.entity.UserInfo;
import com.bizideal.mn.service.UserInfoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 15:16
 * @version: 1.0
 * @Description:
 */
@MyController("controlleeee")
@MyRequestMapping("/user")
public class UserInfoController {

    @MyAutowired
    private UserInfoService userInfoService;

    private AtomicInteger idInc = new AtomicInteger(1);

    @MyRequestMapping("/addUser")
    public void addUser(@MyRequestParam String name, HttpServletRequest request, HttpServletResponse response) {
        userInfoService.addOrUpdateUser(new UserInfo().setName(name).setId(idInc.getAndIncrement()));
        out(response, "add user success");
    }

    @MyRequestMapping("/getUser")
    public void getUser(@MyRequestParam int id, HttpServletRequest request, HttpServletResponse response) {
        UserInfo user = userInfoService.getUser(id);
        out(response, user == null ? "user not found, id = " + id : user.toString());
    }

    private void out(HttpServletResponse response, String str) {
        try {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

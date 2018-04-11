package com.bizideal.mn.annotation;

import java.lang.annotation.*;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 13:53
 * @version: 1.0
 * @Description:
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {

    String value() default "";
}

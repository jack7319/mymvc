package com.bizideal.mn.annotation;

import java.lang.annotation.*;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 15:05
 * @version: 1.0
 * @Description:
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {

    String value() default "";
}

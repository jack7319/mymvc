package com.bizideal.mn.annotation;

import java.lang.annotation.*;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/11 15:07
 * @version: 1.0
 * @Description:
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {

    String value() default "";

    boolean required() default true;
}

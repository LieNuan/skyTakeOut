package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)//指定注解只能用于方法
@Retention(RetentionPolicy.RUNTIME)//固定写法
public @interface AutoFill {
    //定义好的枚举值，UPDATE，INSERT
    OperationType value();
}

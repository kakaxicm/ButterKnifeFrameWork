package com.qicode.kakaxicm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chenming on 2018/7/20
 * 用于javapoet测试的注解
 */
@Retention(RetentionPolicy.CLASS)//编译时注解
@Target(ElementType.TYPE) //标注在属性上
public @interface JPHello {
}

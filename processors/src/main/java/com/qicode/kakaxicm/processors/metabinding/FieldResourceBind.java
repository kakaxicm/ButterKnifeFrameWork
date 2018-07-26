package com.qicode.kakaxicm.processors.metabinding;

import com.squareup.javapoet.MethodSpec;

/**
 * Created by chenming on 2018/7/21
 */
public abstract class FieldResourceBind {
    // 资源ID
    protected int id;
    // 字段变量名称
    protected String name;
    // 获取资源数据的方法
    protected String method;

    public FieldResourceBind(int id, String name, String method) {
        this.id = id;
        this.name = name;
        this.method = method;
    }
    //添加相应的设置属性的语句
    abstract public void handleStatement(MethodSpec.Builder builder);
}

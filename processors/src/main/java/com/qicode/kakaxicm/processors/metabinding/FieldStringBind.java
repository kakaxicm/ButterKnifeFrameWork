package com.qicode.kakaxicm.processors.metabinding;

import com.squareup.javapoet.MethodSpec;

/**
 * Created by chenming on 2018/7/21
 * 构建查找资源语句所需要的参数封装
 */
public class FieldStringBind extends FieldResourceBind {

    public FieldStringBind(int id, String name, String method) {
        super(id, name, method);
    }

    @Override
    public void handleStatement(MethodSpec.Builder builder) {
        builder.addStatement("target.$L = res.$L($L)", name, method, id);
    }
}

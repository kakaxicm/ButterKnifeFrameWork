package com.qicode.kakaxicm.processors.metabinding;

import com.qicode.kakaxicm.processors.BindClass;
import com.squareup.javapoet.MethodSpec;

/**
 * Created by chenming on 2018/7/21
 * 构建查找资源语句所需要的参数封装
 * TODO 结构优化
 */
public class FieldColorBind extends FieldResourceBind {

    public FieldColorBind(int id, String name, String method) {
        super(id, name, method);
    }

    @Override
    public void handleStatement(MethodSpec.Builder builder) {
        builder.addStatement("target.$L = $T.$L(context, $L)", name, BindClass.CONTEXT_COMPAT,
                method, id);
    }

}

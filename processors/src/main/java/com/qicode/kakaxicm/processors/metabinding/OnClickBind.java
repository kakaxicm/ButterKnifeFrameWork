package com.qicode.kakaxicm.processors.metabinding;

import com.qicode.kakaxicm.processors.MethodParameter;

import java.util.List;

/**
 * Created by chenming on 2018/7/26
 */
public final class OnClickBind {
    private String name;
    private List<MethodParameter> parameters;

    public OnClickBind(String name, List<MethodParameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getDescription() {
        return "method '" + name + "'";
    }


    public String getName() {
        return name;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

}

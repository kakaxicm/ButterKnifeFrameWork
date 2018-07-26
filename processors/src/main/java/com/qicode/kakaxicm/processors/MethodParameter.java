package com.qicode.kakaxicm.processors;

/**
 * Created by chenming on 2018/7/26
 */

import com.squareup.javapoet.TypeName;

/** Represents a parameter type and its position in the listener method. */

/**
 * 方法参数的封装
 */
public final class MethodParameter {
    static final MethodParameter[] NONE = new MethodParameter[0];

    private final int listenerPosition;
    private final TypeName type;

    MethodParameter(int listenerPosition, TypeName type) {
        this.listenerPosition = listenerPosition;
        this.type = type;
    }

    int getListenerPosition() {
        return listenerPosition;
    }

    TypeName getType() {
        return type;
    }

    public boolean requiresCast(String toType) {
        return !type.toString().equals(toType);
    }
}


package com.qicode.kakaxicm.butterknife;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by chenming on 2018/7/21
 * 用于同一查找资源，在生成代码中调用Finder的findViewById,getString,getColor等，
 * 对View而言它需要返回View,对其他资源而言，它只需要返回一个Context即可
 */
public enum Finder {
    //绑定对象是View
    VIEW {
        @Override
        public View findView(Object source, int id) {
            return ((View) source).findViewById(id);
        }

        @Override
        public Context getContext(Object source) {
            return ((View) source).getContext();
        }
    },
    //绑定对象是Activity
    ACTIVITY {
        @Override
        public View findView(Object source, int id) {
            return ((Activity) source).findViewById(id);
        }

        @Override
        public Context getContext(Object source) {
            return (Activity) source;
        }
    },
    //绑定对象是对话框
    DIALOG {
        @Override
        public View findView(Object source, int id) {
            return ((Dialog) source).findViewById(id);
        }

        @Override
        public Context getContext(Object source) {
            return ((Dialog) source).getContext();
        }
    };
    //TODO 类型扩充
    /**
     * findViewById
     *
     * @param source
     * @param id
     * @return
     */
    public abstract View findView(Object source, int id);

    /**
     * 获取Context
     *
     * @param source
     * @return
     */
    public abstract Context getContext(Object source);
}

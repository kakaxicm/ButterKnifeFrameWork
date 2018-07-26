package com.qicode.kakaxicm.butterknife;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by chenming on 2018/7/21
 */
public class ButterKnife {
    //key为targetclass, value为ViewBinder对象
    static final Map<Class<?>, ViewBinder<Object>> BINDERS = new LinkedHashMap<>();

    /**
     * 绑定Activity
     * @param activity
     */
    public static void bind(@NonNull Activity activity) {
        bind(activity, activity, Finder.ACTIVITY);
    }

    /**
     * 绑定View
     * @param view
     */
    public static void bind(@NonNull View view) {
        bind(view, view, Finder.VIEW);
    }

    /**
     * 绑定 Dialog
     *
     * @param target 目标为 Dialog
     */
    public static void bind(@NonNull Dialog target) {
        bind(target, target, Finder.DIALOG);
    }

    private static void bind(@NonNull Object target, @NonNull Object source, @NonNull Finder finder) {
        Class<?> targetClass = target.getClass();
        //查找对应的生成类
        ViewBinder<Object> viewBinder = null;
        try {
            viewBinder = findViewBinderForClass(targetClass);
            if (viewBinder != null) {
                viewBinder.bind(finder, target, source);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to bind views for " + targetClass.getName(), e);
        }
    }

    /**
     * 查找ViewBinder
     * @param targetClass
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static ViewBinder<Object> findViewBinderForClass(Class<?> targetClass) throws IllegalAccessException, InstantiationException {
        ViewBinder<Object> viewBinder = BINDERS.get(targetClass);
        if (viewBinder != null) {
            return viewBinder;
        }

        String clsName = targetClass.getName();
        //类名校验,不能绑定系统类
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            return null;
        }

        //用反射和生成类的命名规则查找生成类Class
        try {
            Class<?> viewBindingClass = Class.forName(clsName + "$$ViewBinder");
            viewBinder = (ViewBinder<Object>) viewBindingClass.newInstance();
        } catch (ClassNotFoundException e) {
            //查找父类
            viewBinder = findViewBinderForClass(targetClass.getSuperclass());
        }
        BINDERS.put(targetClass, viewBinder);//存入缓存
        return viewBinder;
    }
}

package com.qicode.kakaxicm.butterknife;

/**
 * Created by chenming on 2018/7/21
 * 生成的类都要实现这个接口，butterKnife执行bind操作的时候也是调用生成的类的bind方法。
 */
public interface ViewBinder<T> {
    /**
     * 绑定操作
     * @param finder 所有生成代码都交给finder执行资源查找操作
     * @param target 绑定的目标对象,根据这个target查找对应生成的类，调用生成类的bind方法，绑定属性
     * @param source 所依附的对象，可能是 target 本身，如果它是 Activity、View、Dialog 的话。
     *               比如我们要进行绑定操作的 target 对象是个 Fragment 的话，
     *               它的 source 就是通过 LayoutInflater.inflate() 返回的View，
     *               它所绑定的视图都是和这个 View关联的，通过这个View 我们才能找到我们的其它视图资源。
     */
    void bind(Finder finder, T target, Object source);
}

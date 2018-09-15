package com.qicode.kakaxicm.processors;

import com.qicode.kakaxicm.processors.metabinding.FieldResourceBind;
import com.qicode.kakaxicm.processors.metabinding.FieldViewBind;
import com.qicode.kakaxicm.processors.metabinding.OnClickBind;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * Created by chenming on 2018/7/21
 * 用于生成代码的数据封装,一个Target宿主类对应一个Bind类
 * 生成的代码如下:
 * public class MainActivity$$ViewBinder<T extends MainActivity> implements ViewBinder<T> {
 *
 * @Override
 * @SuppressWarnings("ResourceType") public void bind(final Finder finder, final T target, Object source) {
 * Context context = finder.getContext(source);
 * Resources res = context.getResources();
 * target.mBindString = res.getString(2131099669);
 * }
 * }
 */


public class BindClass {
    //以下生成的代码需要用到的类
    //Finder类
    public static final ClassName FINDER = ClassName.get("com.qicode.kakaxicm.butterknife", "Finder");
    //ViewBinder接口
    public static final ClassName VIEWBINDER = ClassName.get("com.qicode.kakaxicm.butterknife", "ViewBinder");
    //Context
    public static final ClassName CONTEXT = ClassName.get("android.content", "Context");
    //Resources
    public static final ClassName RESOURCES = ClassName.get("android.content.res", "Resources");
    //ContextCompat
    public static final ClassName CONTEXT_COMPAT = ClassName.get("android.support.v4.content", "ContextCompat");

    //ColorStateList
    public static final String COLOR_STATE_LIST_TYPE = "android.content.res.ColorStateList";//ColorStateList类型
    //View
    public static final ClassName VIEW = ClassName.get("android.view", "View");
    //OnClickListener
    private static final ClassName ON_CLICK_LISTENER = ClassName.get("android.view.View", "OnClickListener");

    //用于生成多条findViewbyId语句
    private List<FieldResourceBind> resourceBindings = new ArrayList<>();
    //一个id只能绑定一个View
    private final Map<Integer, FieldViewBind> fieldViewIdMap = new LinkedHashMap<>();
    //和源码不同，这里规定一个id只能绑定一个方法
    private final Map<Integer, OnClickBind> onClickBindMap = new LinkedHashMap<>();
    private String classPackage;//包名
    private String className;//简单类名
    private String targetClass;//绑定的对象的完全类名
    private String classFqcn;//生成类的全类名

    private BindClass parentBindClass;//如果绑定的类的父类也绑定过，则生成的类也需要继承对应的Bind类

    /**
     * 绑定处理类
     *
     * @param classPackage 包名：com.butterknife
     * @param className    生成的类：MainActivity$$ViewBinder
     * @param targetClass  目标类：com.butterknife.MainActivity
     * @param classFqcn    生成Class的完全限定名称：com.butterknife.MainActivity$$ViewBinder
     */
    public BindClass(String classPackage, String className, String targetClass, String classFqcn) {
        this.classPackage = classPackage;
        this.className = className;
        this.targetClass = targetClass;
        this.classFqcn = classFqcn;
    }

    /**
     * 构建java文件
     *
     * @return
     */
    public JavaFile buildBindingFile() {
        //构建类
        TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T", ClassName.bestGuess(targetClass)));//泛型参数

        if (hasParentBinding()) {//继承父类
            ClassName parentClassName = ClassName.bestGuess(parentBindClass.classFqcn);
            //ParameterizedTypeName为带泛型的类型,第一个入参为类名,第二个为泛型参数
            builder.superclass(ParameterizedTypeName.get(parentClassName,
                    TypeVariableName.get("T")));

        } else {
            //否则实现接口
            ParameterizedTypeName interfaceTypeName = ParameterizedTypeName.get(VIEWBINDER, TypeVariableName.get("T"));
            builder.addSuperinterface(interfaceTypeName);
        }
        //添加方法
        builder.addMethod(createBindMethod());
        return JavaFile.builder(classPackage, builder.build())
                .addFileComment("Generated code from Butter Knife. Do not modify!")
                .build();

    }

    /**
     * 生成bind方法
     *
     * @return
     */
    private MethodSpec createBindMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")//方法名
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(FINDER, "finder", Modifier.FINAL)
                .addParameter(TypeVariableName.get("T"), "target", Modifier.FINAL)
                .addParameter(Object.class, "source");

        if (hasParentBinding()) {
            //执行super方法
            builder.addStatement("super.bind(finder, target, source)");
        }
        //字串处理
        if (hasResourceBinding()) {//如果有资源，则构建查找资源的代码
            //构造Context 和 Resource获取方法
            //过滤警告
            AnnotationSpec annotationSpec = AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "$S", "ResourceType")
                    .build();
            builder.addAnnotation(annotationSpec);

            builder.addStatement("$T context = finder.getContext(source)", CONTEXT);
            builder.addStatement("$T res = context.getResources()", RESOURCES);
            for (FieldResourceBind binding : resourceBindings) {
                binding.handleStatement(builder);
            }
        }

        //构造findView方法
        if (hasViewBinding()) {
            for (Map.Entry<Integer, FieldViewBind> entry : fieldViewIdMap.entrySet()) {
                int id = entry.getKey();
                FieldViewBind fieldViewBind = entry.getValue();
                if (!fieldViewBind.requiresCast()) {//不用强制转换
                    builder.addStatement("target.$L = finder.findView(source, $L)", fieldViewBind.getName(), id);
                } else {
                    builder.addStatement("target.$L = ($T)(finder.findView(source, $L))", fieldViewBind.getName(), fieldViewBind.getType(), id);
                }
            }
        }

        //构造Onclick事件绑定
        if (hasOnClickBinding()) {
            for (Map.Entry<Integer, OnClickBind> entry : onClickBindMap.entrySet()) {
                int id = entry.getKey();
                OnClickBind onClickBind = entry.getValue();
                //先找到View
                builder.addStatement("$T view_$L = finder.findView(source, $L)", VIEW, id, id);
                //判空if else语句
                builder.beginControlFlow("if (view_$L != null)", id);
                //OnClickListener匿名内部类
                TypeSpec.Builder listener = TypeSpec.anonymousClassBuilder("")
                        .superclass(ON_CLICK_LISTENER);
                //实现OnClick方法
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("onClick")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(VIEW, "v");
                //调用绑定目标的方法
                //取方法参数列表
                List<MethodParameter> parameters = onClickBind.getParameters();
                if (parameters.isEmpty()) {
                    //无参
                    methodBuilder.addStatement("target.$L()", onClickBind.getName());
                } else {
                    MethodParameter parameter = parameters.get(0);
                    methodBuilder.addStatement("target.$L(v)", onClickBind.getName());
                }
                listener.addMethod(methodBuilder.build());
                //添加setOnClickListener方法
                builder.addStatement("view_$L.setOnClickListener($L)", id, listener.build());
                builder.endControlFlow();
            }
        }

        return builder.build();
    }

    public void setParentBindClass(BindClass parentBindClass) {
        this.parentBindClass = parentBindClass;
    }

    /**
     * 目标类的父类是否也绑定过
     *
     * @return
     */
    private boolean hasParentBinding() {
        return parentBindClass != null;
    }

    public void addResourceBinding(FieldResourceBind binding) {
        resourceBindings.add(binding);
    }

    public void addFiledViewBinding(int resId, FieldViewBind binding) {
        FieldViewBind fieldViewBind = fieldViewIdMap.get(resId);
        if (fieldViewBind == null) {
            fieldViewIdMap.put(resId, binding);
        }
    }

    public FieldViewBind getExistingFiledViewBinding(int resId) {
        return fieldViewIdMap.get(resId);
    }

    private boolean hasResourceBinding() {
        return !resourceBindings.isEmpty();
    }

    private boolean hasViewBinding() {
        return !fieldViewIdMap.isEmpty();
    }

    public void addOnClickBind(int id, OnClickBind onClickBind) {
        OnClickBind bind = onClickBindMap.get(id);
        if (bind == null) {
            onClickBindMap.put(id, onClickBind);
        }
    }

    public OnClickBind getExistingOnClickBinding(int viewId) {
        return onClickBindMap.get(viewId);
    }

    private boolean hasOnClickBinding() {
        return !onClickBindMap.isEmpty();
    }
}

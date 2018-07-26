package com.qicode.kakaxicm.processors;

import com.qicode.kakaxicm.annotations.BindColor;
import com.qicode.kakaxicm.annotations.BindString;
import com.qicode.kakaxicm.annotations.BindView;
import com.qicode.kakaxicm.annotations.OnClick;
import com.qicode.kakaxicm.processors.metabinding.FieldColorBind;
import com.qicode.kakaxicm.processors.metabinding.FieldStringBind;
import com.qicode.kakaxicm.processors.metabinding.FieldViewBind;
import com.qicode.kakaxicm.processors.metabinding.OnClickBind;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;

/**
 * Created by chenming on 2018/7/21
 * 解析注解，构造Binding
 */
public class BindParserUtils {
    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";

    /**
     * 解析资源类型的注解，构造BindClass
     *
     * @param element
     * @param bindClassMap
     * @param typesSet
     * @param elementUtils
     */
    public static void parseStringRes(Element element, Map<TypeElement, BindClass> bindClassMap,
                                      Set<TypeElement> typesSet, Elements elementUtils) {
        //构造BindClass
        BindClass bindClass = getOrBuildBindClass(element, bindClassMap, elementUtils);

        //构造getString语句所需要的参数
        //获得属性名称
        String fieldName = element.getSimpleName().toString();
        //拿到注解设置的资源ID
        int fieldResId = element.getAnnotation(BindString.class).value();
        System.out.println("===字串属性名称:"+fieldName+" ,id="+fieldResId);
        //将参数封装到FieldResourceBinding
        FieldStringBind fieldStringBind = new FieldStringBind(fieldResId, fieldName, "getString");
        //相当于添加一条语句
        bindClass.addResourceBinding(fieldStringBind);
        //外围类加入集合
        typesSet.add((TypeElement) element.getEnclosingElement());
    }

    /**
     * 解析颜色注解的元素
     * @param element
     * @param bindClassMap
     * @param typesSet
     * @param elementUtils
     */
    public static void parseColorRes(Element element, Map<TypeElement, BindClass> bindClassMap,
                                     Set<TypeElement> typesSet, Elements elementUtils){
        //构造BindClass
        BindClass bindClass = getOrBuildBindClass(element, bindClassMap, elementUtils);
        //构造getString语句所需要的参数
        //获得属性名称
        String fieldName = element.getSimpleName().toString();
        //拿到注解设置的资源ID
        int fieldResId = element.getAnnotation(BindColor.class).value();
        String methodName;
        if(BindClass.COLOR_STATE_LIST_TYPE.equals(element.asType().toString())){
            methodName = "getColorStateList";
        }else{
            methodName = "getColor";
        }
        FieldColorBind fieldColorBind = new FieldColorBind(fieldResId,fieldName, methodName);
        bindClass.addResourceBinding(fieldColorBind);
        //外围类加入集合
        typesSet.add((TypeElement) element.getEnclosingElement());
    }

    /**
     * 解析BindView注解的元素
     * @param element
     * @param bindClassMap
     * @param typesSet
     * @param elementUtils
     */
    public static void parseView(Element element, Map<TypeElement, BindClass> bindClassMap,
                                 Set<TypeElement> typesSet, Elements elementUtils, Messager messager){
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        TypeMirror elementType = element.asType();
        //构造BindClass
        BindClass bindClass = getOrBuildBindClass(element, bindClassMap, elementUtils);
        //构造getString语句所需要的参数
        //获得属性名称
        String fieldName = element.getSimpleName().toString();
        //拿到注解设置的资源ID
        int fieldResId = element.getAnnotation(BindView.class).value();
        //取得viewfield的类型
        TypeName type = TypeName.get(elementType);

        FieldViewBind existViewBinding = bindClass.getExistingFiledViewBinding(fieldResId);
        if(existViewBinding != null){
            // 存在重复使用的ID
            VerifyUtils.error(messager, element, "Attempt to use @%s for an already bound ID %d on '%s'. (%s.%s)",
                    BindView.class.getSimpleName(), fieldResId, existViewBinding.getName(),
                    enclosingElement.getQualifiedName(), element.getSimpleName());
        }
        //TODO 构造fieldViewBind，加入到BindClass
        FieldViewBind fieldViewBind = new FieldViewBind(fieldName, type, true);
        bindClass.addFiledViewBinding(fieldResId, fieldViewBind);

        //目标类加入集合
        typesSet.add(enclosingElement);
    }

    /**
     * 校验通过,解析OnClick注解的方法
     * @param element
     * @param bindClassMap
     * @param typesSet
     * @param elementUtils
     * @param messager
     */
    public static void parseOnclick(Element element, Map<TypeElement, BindClass> bindClassMap,
                                 Set<TypeElement> typesSet, Elements elementUtils, Messager messager){
        ExecutableElement executableElement = (ExecutableElement) element;
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        //构造BindClass
        BindClass bindClass = getOrBuildBindClass(element, bindClassMap, elementUtils);
        //OnClick方法所需要的参数
        //取id列表
        int[] ids = element.getAnnotation(OnClick.class).value();
        //取方法名称
        String name = executableElement.getSimpleName().toString();
        List<? extends VariableElement> methodParameters = executableElement.getParameters();

        //封装自己的参数类型，然后构造methodbinding
        MethodParameter[] parameters = MethodParameter.NONE;
        if(!methodParameters.isEmpty()){
            parameters = new MethodParameter[1];
            //取参数类型
            TypeMirror typeMirror = methodParameters.get(0).asType();
            //泛型处理
            if(typeMirror instanceof TypeVariable){
                TypeVariable typeVariable = (TypeVariable) typeMirror;
                typeMirror = typeVariable.getUpperBound();
            }
            parameters[0] = new MethodParameter(0, TypeName.get(typeMirror));
        }

        OnClickBind onClickBind = new OnClickBind(name, Arrays.asList(parameters));
        for(int id: ids){
            //和源码不同，这里强制一个View只能绑定一个事件方法
            if(bindClass.getExistingOnClickBinding(id) != null){
                VerifyUtils.error(messager, element, "Attempt to use @%s for an already bound ID %d on '%s'. (%s.%s)",
                        OnClick.class.getSimpleName(), id, bindClass.getExistingOnClickBinding(id).getName(),
                        enclosingElement.getQualifiedName(), element.getSimpleName());
            }
            bindClass.addOnClickBind(id, onClickBind);
        }

        typesSet.add(enclosingElement);
    }

    private static BindClass getOrBuildBindClass(Element element, Map<TypeElement, BindClass> bindClassMap, Elements elementUtils) {
        //拿到外围类
        TypeElement targetTypeElement = (TypeElement) element.getEnclosingElement();

        BindClass bindClass = bindClassMap.get(targetTypeElement);
        if (bindClass != null) {
            return bindClass;
        }
        //新建,构造bind类的包名，简单类名，和完全目标类名，进而构造bind类的完全类名,生成的类和目标类在同一个包下。

        // 获取元素的完全限定名称：com.butterknife.MainActivity
        String targetClassName = targetTypeElement.getQualifiedName().toString();
        //获取包名
        // 获取元素所在包名：com.butterknife
        String pckName = elementUtils.getPackageOf(element).getQualifiedName().toString();

        int excludePckIndex = pckName.length() + 1;
        //bind类的简单名
        String bindClassSimpleName = targetClassName.substring(excludePckIndex).replace('.', '$') + BINDING_CLASS_SUFFIX;

        String bindClassFullName = pckName + "." + bindClassSimpleName;
        System.out.println("===pckName="+pckName+" ,bindClassSimpleName="+bindClassSimpleName+", targetClassName="+targetClassName+" ,bindClassFullName="+bindClassFullName);
        bindClass = new BindClass(pckName, bindClassSimpleName, targetClassName, bindClassFullName);
        bindClassMap.put(targetTypeElement, bindClass);
        return bindClass;
    }
}

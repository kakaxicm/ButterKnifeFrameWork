package com.qicode.kakaxicm.processors;

import com.google.auto.common.SuperficialValidation;
import com.qicode.kakaxicm.annotations.BindColor;
import com.qicode.kakaxicm.annotations.BindString;
import com.qicode.kakaxicm.annotations.BindView;
import com.qicode.kakaxicm.annotations.OnClick;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;

/**
 * Created by chenming on 2018/7/21
 */
public class VerifyUtils {

    private static final String STRING_TYPE = "java.lang.String";//Field为String类型
    private static final String COLOR_STATE_LIST_TYPE = "android.content.res.ColorStateList";//ColorStateList类型
    public static final String VIEW_TYPE = "android.view.View";
    public static final String OBJECT_TYPE = "java.lang.Object";
    private static final String LIST_TYPE = List.class.getCanonicalName();
    private static final String ITERABLE_TYPE = "java.lang.Iterable<?>";

    /**
     * 校验BindString注解的元素
     *
     * @param element
     * @param messager
     * @return
     */
    public static boolean verifyResString(Element element, Messager messager) {
        verifyRes(element, BindString.class, messager);
        return true;
    }

    /**
     * BindColor
     *
     * @param element
     * @param messager
     * @return
     */
    public static boolean verifyResColor(Element element, Messager messager) {
        verifyRes(element, BindColor.class, messager);
        return true;
    }

    /**
     * 校验BindString注解的元素
     *
     * @param element
     * @param messager
     * @return
     */
    public static boolean verifyResView(Element element, Messager messager) {
        verifyRes(element, BindView.class, messager);
        return true;
    }

    /**
     * 校验BindString注解的元素
     *
     * @param element
     * @param messager
     * @return
     */
    public static boolean verifyMethodOnclick(Element element, Messager messager) {
        verifyRes(element, OnClick.class, messager);
        return true;
    }

    /**
     * 校验Filed元素的模板流程
     *
     * @param element
     * @param annotationClass
     * @param messager
     * @return
     */
    private static boolean verifyRes(Element element, Class<? extends Annotation> annotationClass, Messager messager) {
        // 检测元素的有效性
        if (!SuperficialValidation.validateElement(element)) {
            return false;
        }

        //校验元素对应的java类型的合法性,不同的注解针对不同的类型
        if (!verifyElementType(element, annotationClass, messager)) {
            return false;
        }
        //获取外围元素,即父节点
        TypeElement parentElement = (TypeElement) element.getEnclosingElement();

        // 使用该注解的字段访问权限不能为 private 和 static
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            error(messager, element, "@%s %s must not be private or static. (%s.%s)",
                    annotationClass.getSimpleName(), "fields", parentElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }

        // 包含该注解的外围元素种类必须为 Class
        if (parentElement.getKind() != ElementKind.CLASS) {
            error(messager, parentElement, "@%s %s may only be contained in classes. (%s.%s)",
                    annotationClass.getSimpleName(), "fields", parentElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }

        // 包含该注解的外围元素访问权限不能为 private
        if (parentElement.getModifiers().contains(Modifier.PRIVATE)) {
            error(messager, parentElement, "@%s %s may not be contained in private classes. (%s.%s)",
                    annotationClass.getSimpleName(), "fields", parentElement.getQualifiedName(),
                    element.getSimpleName());
            return false;
        }

        // 判断是否处于错误的包中
        String qualifiedName = parentElement.getQualifiedName().toString();
        if (qualifiedName.startsWith("android.")) {
            error(messager, element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return false;
        }
        if (qualifiedName.startsWith("java.")) {
            error(messager, element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return false;
        }


        return true;
    }

    /**
     * 针对不同的注解，Field是不同的类型
     *
     * @param element
     * @param annotationClass
     * @param messager
     * @return
     */
    private static boolean verifyElementType(Element element, Class<? extends Annotation> annotationClass, Messager messager) {
        // 获取最里层的外围元素
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        //校验String类型
        TypeMirror elementType = element.asType();
        if (annotationClass == BindString.class) {
            // 检测使用该注解的元素类型是否正确
            if (!STRING_TYPE.equals(elementType.toString())) {
                error(messager, element, "@%s field type must be 'String'. (%s.%s)",
                        annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
                        element.getSimpleName());
                return false;
            }
        }
        //校验Color类型
        if (annotationClass == BindColor.class) {
            if (COLOR_STATE_LIST_TYPE.equals(elementType.toString())) {
                return true;
            } else if (elementType.getKind() != TypeKind.INT) {
                error(messager, element, "@%s field type must be 'int' or 'ColorStateList'. (%s.%s)",
                        BindColor.class.getSimpleName(), enclosingElement.getQualifiedName(),
                        element.getSimpleName());
                return false;
            }
        }

        if (annotationClass == BindView.class) {
            //TODO 检测BindView元素
            // Verify that the target type extends from View.
            if (elementType.getKind() == TypeKind.TYPEVAR) {//如果是泛型,则找到它的上边界
                TypeVariable typeVariable = (TypeVariable) elementType;
                elementType = typeVariable.getUpperBound();
            }
            //如果不是View的子类且不是接口则返回false
            if (!isSubtypeOfType(elementType, VIEW_TYPE) && !isInterface(elementType)) {
                if (elementType.getKind() == TypeKind.ERROR) {
                    error(messager, element, "@%s field with unresolved type (%s) "
                            + "must elsewhere be generated as a View or interface. (%s.%s)", BindView.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());
                } else {
                    error(messager, element, "@%s fields must extend from View or be an interface. (%s.%s)",
                            BindView.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());
                }
                return false;
            }
            return true;
        }
        //校验Onclick方法
        if (annotationClass == OnClick.class) {
            //判断是元素否为方法
            if (!(element instanceof ExecutableElement) || (element.getKind() != METHOD)) {
                error(messager, element, "@%s annotation must be on a method.", annotationClass.getSimpleName());
                return false;
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            //取注解viewid
            int[] ids = element.getAnnotation(OnClick.class).value();
            String name = executableElement.getSimpleName().toString();
            //id是否重复
            Integer duplicate = findDuplicate(ids);
            if (duplicate != null) {
                error(messager, element, "@%s annotation contains duplicate ID %d. (%s.%s)", OnClick.class.getSimpleName(),
                        duplicate, enclosingElement.getQualifiedName(), element.getSimpleName());
                return false;

            }

            for (int id : ids) {
                //如果没有设置id,则把外围类作为要设置点击的对象
                if (id == -1) {
                    if (ids.length > 1) {//外围类作为点击对象，绑定的view只能是他自己
                        error(messager, element, "@%s annotation contains invalid ID %d. (%s.%s)",
                                OnClick.class.getSimpleName(), -1, enclosingElement.getQualifiedName(),
                                element.getSimpleName());
                        return false;
                    }

                    //判断外围类是否是View的子类或者接口
                    if (!isSubtypeOfType(enclosingElement.asType(), VIEW_TYPE) && !isInterface(enclosingElement.asType())) {
                        error(messager, element, "@%s annotation without an ID may only be used with an object of type "
                                        + "\"%s\" or an interface. (%s.%s)",
                                OnClick.class.getSimpleName(), VIEW_TYPE,
                                enclosingElement.getQualifiedName(), element.getSimpleName());
                        return false;
                    }

                }
            }

            List<? extends VariableElement> parameters = executableElement.getParameters();
            //方法参数最多一个View
            if (parameters.size() > 1) {
                error(messager, element, "@%s methods can have at most 1 parameter(s). (%s.%s)",
                        OnClick.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());
                return false;
            }

            if (!parameters.isEmpty()) {
                TypeMirror typeMirror = parameters.get(0).asType();
                //泛型处理
                if(typeMirror instanceof TypeVariable){
                    TypeVariable typeVariable = (TypeVariable) typeMirror;
                    typeMirror = typeVariable.getUpperBound();
                }
                //必须为View的子类或者接口
                if(!isSubtypeOfType(typeMirror, VIEW_TYPE) && isInterface(typeMirror)){
                    error(messager, element, "Unable to match @%s  method arguments. (%s.%s)",
                            OnClick.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());
                    return false;
                }

            }
            //返回值类型必须为void
            TypeMirror returnType = executableElement.getReturnType();

            //判断返回值是否为Void
            //泛型处理
            if (returnType instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) returnType;
                returnType = typeVariable.getUpperBound();
            }

            if (returnType.getKind() != TypeKind.VOID) {
                error(messager, element, "@%s methods must have a 'void' return type. (%s.%s)",
                        OnClick.class.getSimpleName(), enclosingElement.getQualifiedName(), element.getSimpleName());

                return false;
            }

        }

        //TODO 校验其他类型
        return true;
    }

    /**
     * Returns the first duplicate element inside an array, null if there are no duplicates.
     */
    private static Integer findDuplicate(int[] array) {
        Set<Integer> seenElements = new LinkedHashSet<>();

        for (int element : array) {
            if (!seenElements.add(element)) {
                return element;
            }
        }

        return null;
    }

    /**
     * 判断是否是接口元素
     *
     * @param typeMirror
     * @return
     */
    private static boolean isInterface(TypeMirror typeMirror) {
        return (typeMirror instanceof DeclaredType) && ((DeclaredType) typeMirror).asElement().getKind() == INTERFACE;
    }

    /**
     * typeMirror是否是otherType的子类
     *
     * @param typeMirror
     * @param otherType
     * @return
     */
    private static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        //如果类型相同则返回true
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }

        //不是声明的类型返回false
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }

        DeclaredType declaredType = (DeclaredType) typeMirror;
        System.out.println("======校验元素类型:" + declaredType.asElement().toString());
        //如果typeMirror带泛型参数，则拼接带泛型类名判等
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        //向上递归检查
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {//如果不是类型元素返回false
            return false;
        }
        //查找父类或者接口
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superclass = typeElement.getSuperclass();
        System.out.println("======校验元素父类类型:" + declaredType.asElement().toString());
        if (isSubtypeOfType(superclass, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }


    /**
     * 输出错误信息
     *
     * @param element
     * @param message
     * @param args
     */
    public static void error(Messager messager, Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

}

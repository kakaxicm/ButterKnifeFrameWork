package com.qicode.kakaxicm.processors;

import com.google.auto.service.AutoService;
import com.qicode.kakaxicm.annotations.BindString;
import com.qicode.kakaxicm.annotations.TestAnnotation;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by chenming on 2018/7/20
 */
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    private Types typeUtils;//用于处理Element对应的java类型
    private Elements elementUtils;//用于处理代码文档元素
    private Messager messager;//控制台输出日志
    /**
     * init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
     * ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
     *
     * @param processingEnv 提供给 processor 用来访问工具框架的环境,相当于上下文
     */

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    /**
     * 核心方法，这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
     * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
     *
     * @param annotations 请求处理的注解类型
     * @param roundEnv    有关当前和以前的信息环境
     * @return 如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们；
     * 如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
     */

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsSet = roundEnv.getElementsAnnotatedWith(TestAnnotation.class);
        for (Element element : elementsSet) {

            System.out.println("------------------------------");
            if (element.getKind() == ElementKind.CLASS) {//如果元素为类的element
                TypeElement typeElement = (TypeElement) element;
                //拿到typeElement，可以提取它的信息了，类似于解析xml节点元素
                String classSimpleName = typeElement.getSimpleName().toString();//拿到类简称
                String classQualifiedName = typeElement.getQualifiedName().toString();//拿到类的全名
                String value = element.getAnnotation(TestAnnotation.class).value();
                System.out.println("classSimpleName=" + classSimpleName);
                System.out.println("classQualifiedName=" + classQualifiedName);
                System.out.println("value=" + value);


                //拿到包元素信息
                PackageElement packageElement = elementUtils.getPackageOf(element);
                String fullPckName = packageElement.getQualifiedName().toString();
                String simplePckName = packageElement.getSimpleName().toString();
                System.out.println("===============");
                System.out.println("simplePckName:"+simplePckName);
                System.out.println("fullPckName:"+fullPckName);
                //打印：
                // simplePckName:annotationprocessdr
                // fullPckName:com.qicode.kakaxicm.annotationprocessdr


            }
            System.out.println("------------------------------");


        }


        Set<? extends Element> bindStringElementsSet = roundEnv.getElementsAnnotatedWith(BindString.class);
        for (Element element : bindStringElementsSet) {
            //拿到上层元素信息，对Field注解而言，上层就是TypeElement,类元素信息
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String simpleClassName = enclosingElement.getSimpleName().toString();
            String fullClassName = enclosingElement.getQualifiedName().toString();
            System.out.println("===============");
            System.out.println("simpleClassName:"+simpleClassName);
            System.out.println("fullClassName:"+fullClassName);
            System.out.println("===============");
            //打印:
            //simpleClassName:MainActivity
            //fullClassName:com.qicode.kakaxicm.annotationprocessdr.MainActivity
        }
        return false;
    }

    /**
     * 这里必须指定，这个注解处理器是注册给哪些注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称
     *
     * @return 注解器所支持的注解类型集合，如果没有这样的类型，则返回一个空集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        String annoName = TestAnnotation.class.getCanonicalName();
        System.out.println("-------------getSupportedAnnotationTypes-----------------");
        System.out.println("-------------annoName:" + annoName + "-----------------");
        types.add(annoName);
        String bindStringName = BindString.class.getCanonicalName();
        types.add(bindStringName);
        return types;
    }

    /**
     * 指定使用的Java版本，通常这里返回SourceVersion.latestSupported()
     *
     * @return 使用的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
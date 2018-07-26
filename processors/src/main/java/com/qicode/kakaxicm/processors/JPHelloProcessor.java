package com.qicode.kakaxicm.processors;

import com.google.auto.service.AutoService;
import com.qicode.kakaxicm.annotations.JPHello;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by chenming on 2018/7/20
 */
@AutoService(Processor.class)
public class JPHelloProcessor extends AbstractProcessor {
    private Filer filer;//用于创建java文件
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsSet = roundEnv.getElementsAnnotatedWith(JPHello.class);
        for (Element element : elementsSet) {

            System.out.println("--------------开始创建java文件----------------");
            if (element.getKind() == ElementKind.CLASS) {//如果元素为类的element
                TypeElement typeElement = (TypeElement) element;
                //写java文件
                //创建main方法
                MethodSpec mainMethod = MethodSpec.methodBuilder("main")//方法名
                        .addModifiers(Modifier.PUBLIC,Modifier.STATIC)//方法修饰符
                        .returns(void.class)//返回类型
                        .addParameter(String[].class, "args")//参数类型和参数名称
                         //语句，第一个参数表示语句模板，$T、$S表示各自的占位符，有各自的意义
                        .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                        .build();
                //创建类
                TypeSpec helloClass = TypeSpec.classBuilder("JPHello")//类名
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)//修饰符
                        .addMethod(mainMethod)//添加方法
                        .build();
                //指定类的包名，添加注释
                JavaFile javaFile = JavaFile.builder("com.test", helloClass)//第一个参数为包名
                        .addFileComment("This codes are generated automatically. Do not modify!")
                        .build();
                try {
                    //正式写java文件
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    System.out.println("--------------创建java文件失败----------------");
                    System.out.println(e.getLocalizedMessage());
                }
                System.out.println("-------------创建结束-----------------");
                break;
            }



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
        String annoName = JPHello.class.getCanonicalName();
        System.out.println("-------------getSupportedAnnotationTypes-----------------");
        System.out.println("-------------annoName:" + annoName + "-----------------");
        types.add(annoName);
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
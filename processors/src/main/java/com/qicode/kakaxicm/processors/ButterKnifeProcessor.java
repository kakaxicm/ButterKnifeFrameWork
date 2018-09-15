package com.qicode.kakaxicm.processors;

import com.google.auto.service.AutoService;
import com.qicode.kakaxicm.annotations.BindColor;
import com.qicode.kakaxicm.annotations.BindString;
import com.qicode.kakaxicm.annotations.BindView;
import com.qicode.kakaxicm.annotations.OnClick;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by chenming on 2018/7/20
 */
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {
    private Filer filer;//用于创建java文件
    private Types typeUtils;//用于处理Element对应的java类型
    private Elements elementUtils;//用于处理代码文档元素
    private Messager messager;//控制台输出日志

    private Set<TypeElement> erasedTargetTypes = new HashSet<>();//保存所有曾经处理过的外围类
    //外围类到对应BindClass的映射
    private Map<TypeElement, BindClass> targetoBindClassMap = new LinkedHashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //处理BindString注解元素
        //Element 表示代码元素
        Set<? extends Element> elementsWithBindString = roundEnv.getElementsAnnotatedWith(BindString.class);
        for (Element element : elementsWithBindString) {
            //校验字串元素
            if(VerifyUtils.verifyResString(element, messager)){
                //解析
                BindParserUtils.parseStringRes(element, targetoBindClassMap, erasedTargetTypes, elementUtils);
            }

        }

        //处理BindColor注解元素
        Set<? extends Element> elementsWithBindColor = roundEnv.getElementsAnnotatedWith(BindColor.class);
        for (Element element : elementsWithBindColor) {
            //校验颜色元素
            if(VerifyUtils.verifyResColor(element, messager)){
                //解析颜色元素
                BindParserUtils.parseColorRes(element, targetoBindClassMap, erasedTargetTypes, elementUtils);
            }

        }

        //处理BindView注解元素
        Set<? extends Element> elementsWithBindView = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elementsWithBindView) {
            //校验View元素
            if(VerifyUtils.verifyResView(element, messager)){
                System.out.println("=======View元素校验通过=========");
                //解析颜色元素
                BindParserUtils.parseView(element, targetoBindClassMap, erasedTargetTypes, elementUtils, messager);
            }

        }

        //处理BindView注解元素
        Set<? extends Element> elementsWithBindOnClick = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : elementsWithBindOnClick) {
            //校验onClick元素
            if(VerifyUtils.verifyMethodOnclick(element, messager)){
                System.out.println("=======Onclick方法校验通过=========");
                //TODO 解析onclick注解的方法
                BindParserUtils.parseOnclick(element, targetoBindClassMap, erasedTargetTypes, elementUtils, messager);
            }

        }

        //TODO 处理其他注解类型
        System.out.println("targetoBindClassMap大小:"+targetoBindClassMap.size() + ",erasedTargetTypes大小:"+erasedTargetTypes.size());

        //开始输出java文件
        for (Map.Entry<TypeElement, BindClass> entry : targetoBindClassMap.entrySet()) {

            TypeElement typeElement = entry.getKey();
            BindClass bindingClass = entry.getValue();

            //输出之前，先检查target类是否有父类被绑定过，如果有则需要继承对应的bind类
            TypeElement parentElement = getParentElement(typeElement);
            if (parentElement != null) {
                BindClass parentBindClass = targetoBindClassMap.get(parentElement);
                //子类bind集成父类bind
                bindingClass.setParentBindClass(parentBindClass);
            }

            //生成java文件
            try {
                bindingClass.buildBindingFile().writeTo(filer);
            } catch (IOException e) {
                error(typeElement, "Unable to write view binder for type %s: %s", typeElement,
                        e.getMessage());

            }

        }
        //process的循环执行次数和getSupportedAnnotationTypes返回的集合size一致，说明可能会执行多次，
        //为了避免重复创建文件，这里清除掉集合
        targetoBindClassMap.clear();
        erasedTargetTypes.clear();
        return true;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }


    /**
     * 查找已经处理过的父类TypeElement
     * @param typeElement
     * @return
     */
    private TypeElement getParentElement(TypeElement typeElement) {
        TypeMirror superclass = null;
        //向上遍历继承树
        while (true) {
            superclass = typeElement.getSuperclass();
            if (superclass.getKind() == TypeKind.NONE) {
                return null;
            }
            //获得父类的TypeElment
            typeElement = (TypeElement) ((DeclaredType) superclass).asElement();
            if (erasedTargetTypes.contains(typeElement)) {
                return typeElement;
            }
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(BindString.class.getCanonicalName());
        types.add(BindColor.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
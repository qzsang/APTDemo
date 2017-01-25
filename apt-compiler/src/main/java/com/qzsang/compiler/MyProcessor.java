package com.qzsang.compiler;

import com.google.auto.service.AutoService;
import com.qzsang.annotation.ViewInject;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

    /**
     * 使用 Google 的 auto-service 库可以自动生成 META-INF/services/javax.annotation.processing.Processor 文件
     */

    private Filer mFiler; //文件相关的辅助类
    private Elements mElementUtils; //元素相关的辅助类
    private Messager mMessager; //日志相关的辅助类


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();

    }

    /**
     * @return 指定哪些注解应该被注解处理器注册
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(ViewInject.class.getCanonicalName());
        return types;
    }

    /**
     * @return 指定使用的 Java 版本。通常返回 SourceVersion.latestSupported()。
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //取值
        //得到 ViewInject 注解的参数集  并循环取得对应值存入 variableElements 与 ViewIds 中
        List<VariableElement> variableElements = new ArrayList<>();
        List<Integer> viewIds = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(ViewInject.class)) {
            VariableElement variableElement = null;
            int viewId = 0;
            try {
                variableElement = (VariableElement) element;//得到参数
                ViewInject viewInject = variableElement.getAnnotation(ViewInject.class);//得到viewInject注解类
                viewId = viewInject.value();

            } catch (Exception e) {
                error("%s 注解无法解析", ViewInject.class.getSimpleName());
            }

            if (viewId < 0) {
                error("%s 的Id值不能小于0", ViewInject.class.getSimpleName());
            }

            variableElements.add(variableElement);
            viewIds.add(viewId);
        }


        //准备工作 得到类的Element
        Element parentElement = null;
        if (variableElements.size() > 0 ) {
            parentElement = variableElements.get(0).getEnclosingElement();
        } else {
            return true;
        }
        //生成代码
        /*
        public void init (final Activity activity)  {
            for () {
                findViewById
            }
        }
        */


        MethodSpec.Builder builder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.get(parentElement.asType()),"activity",Modifier.FINAL);
        //for
        for (int i = 0;i < variableElements.size();i++) {
            VariableElement variableElement = variableElements.get(i);
            builder.addCode(
                    "activity." +
                            variableElement.getSimpleName() +
                            " = " +
                            "(" + TypeName.get(variableElement.asType()) + ")" +
                            "activity.findViewById(" +
                            viewIds.get(i) +
                            ");\n"
            );
        }
        MethodSpec init = builder.build();

        //设置类
        TypeSpec classHolder = TypeSpec.classBuilder(parentElement.getSimpleName() + "$$Holder")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(init)
                .build();
        //生成java源码
        //得到类的包路径  为了包路径保持一致
        String packageName = mElementUtils.getPackageOf(parentElement).getQualifiedName().toString();
        //生成java 代码
        JavaFile javaFile = JavaFile.builder(packageName, classHolder)
                .build();
        try {
            javaFile.writeTo(mFiler);//写入
        } catch (IOException e) {
            e.printStackTrace();
            error("生成代码失败, 原因: %s", e.getMessage());
        }
        return true;
    }


    private void error(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }

    private void info(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format(msg, args));
    }
}

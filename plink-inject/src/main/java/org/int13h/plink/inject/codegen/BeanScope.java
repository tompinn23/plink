package org.int13h.plink.inject.codegen;

import com.palantir.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

public class BeanScope {

    private final Map<String, BeanReader> beanNames;
    private final List<BeanReader> beans;

    private final String scopeName;
    private boolean written;


    private final Map<BeanReader, BeanWriter> beanWriters = new HashMap<>();

    public BeanScope() {
        this.scopeName = "Singletons";
        this.beanNames = new HashMap<>();
        this.beans = new ArrayList<>();
    }

    public List<BeanReader> getBeans() {
        return beans;
    }

    public Map<String, BeanReader> getBeanNames() {
        return beanNames;
    }

    public void addBeanDefinition(BeanReader beanReader) {
        for(String provides : beanReader.provideNames()) {
            if(beanNames.containsKey(provides) && !(beanNames.get(provides).isOptional() && beanReader.isOptional())) {
                throw new IllegalStateException(String.format("Duplicate bean registration %s provides %s already", beanNames.get(provides).type(), provides));
            }
            beanNames.put(provides, beanReader);
        }
        beans.add(beanReader);
    }

    public Optional<BeanReader> getByClass(String className) {
        return Optional.ofNullable(beanNames.get(className));
    }

    public BeanWriter getBeanWriter(BeanReader reader) {
        return beanWriters.computeIfAbsent(reader, r -> {
            try {
                return new BeanWriter(r);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public MethodSpec writeProvides() {
        var builder = MethodSpec.methodBuilder("provides")
                .returns(ArrayTypeName.of(Class.class))
                .addModifiers(Modifier.PUBLIC);
        builder.addCode("return new Class<?>[] {\n");

        for(BeanReader reader : beans) {
            for(TypeElement provides : reader.provides()) {
                builder.addCode("\t$T.class,\n", TypeName.get(provides.asType()));
            }
        }
        builder.addCode("};\n");
        return builder.build();
    }

    public MethodSpec writeClasses() {
        var builder = MethodSpec.methodBuilder("classes")
                .returns(ArrayTypeName.of(Class.class))
                .addModifiers(Modifier.PUBLIC);

        builder.addCode("return new Class<?>[] {\n");

        for(BeanReader bean : beans) {
            builder.addCode("\t$T.class,\n", bean.typeName());
        }

        builder.addCode("};\n");

        return builder.build();
    }

    public MethodSpec writeBeanMethod(TypeName builderType, BeanReader reader, BeanWriter writer) {

        TypeName beanContainer = TypeName.get(APContext.typeElement("org.int13h.plink.inject.BeanDefinition").asType());
        var beanMeta = APContext.typeElement("org.int13h.plink.inject.BeanMeta");

        var anno = AnnotationSpec.builder(ClassName.get(beanMeta));

        writer.writeBeanMeta(anno);

        return MethodSpec.methodBuilder("build_" + reader.simpleName())
                .returns(TypeName.VOID)
                .addParameter(builderType, "builder")
                .addStatement("$T.build(builder)", writer.typeName().get())
                .addAnnotation(anno.build()).build();
    }

    public MethodSpec writeMeta(TypeName builder) {
        var beanMeta = TypeName.get(APContext.typeElement("org.int13h.plink.inject.BeanMeta").asType());
        return MethodSpec.methodBuilder("getMeta")
                .addModifiers(Modifier.PRIVATE)
                .returns(beanMeta)
                .addParameter(String.class, "name")
                .beginControlFlow("try")
                .addStatement("$T method = getClass().getMethod(name, $T.class)", TypeName.get(Method.class), builder)
                .addStatement("return method.getAnnotation($T.class)", beanMeta)
                .nextControlFlow("catch ($T ignored)", NoSuchMethodException.class)
                .addStatement("return null")
                .endControlFlow()
                .build();
    }

    public boolean write(boolean processingOver) throws IOException {
        if(this.written) return true;
        String top = beans.getFirst().packageName();
        for(BeanReader reader : beans) {
            top = Utils.commonPackage(top, reader.packageName());
        }
        for(var writer : beans.stream().map(this::getBeanWriter).toList()) {
            if(writer.shouldDelay()) {
                return false;
            }
            writer.write();
        }

        TypeName container = TypeName.get(APContext.typeElement("org.int13h.plink.inject.BeanContainer").asType());
        TypeName builderType = TypeName.get(APContext.typeElement("org.int13h.plink.inject.Builder").asType());

        var beanMethods = new ArrayList<MethodSpec>();
        for(var bean : beans) {
            if(getBeanWriter(bean).typeName().isEmpty()) { /* we need to wait for the next round */
                return false;
            }
            beanMethods.add(writeBeanMethod(builderType, bean, getBeanWriter(bean)));
        }

        var order = new BeanGraph(this);


        MethodSpec.Builder builder = MethodSpec.methodBuilder("build")
                .returns(TypeName.VOID)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builderType, "builder");

        for(var bean : order.getBeanOrder()) {
            var writer = getBeanWriter(bean);
            if(writer.typeName().isEmpty()) { /* we need to wait for the next round */
                return false;
            }
            builder.addStatement("build_" + bean.simpleName() + "(builder)");
        }

        var buildMethod = builder.build();

        TypeSpec clazz = TypeSpec.classBuilder("BeanContainer$" + this.scopeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(container)
                .addMethod(writeClasses())
                .addMethod(writeProvides())
                .addMethod(buildMethod)
                .addMethods(beanMethods)
                .addMethod(writeMeta(builderType))
                .build();

        JavaFile file = JavaFile.builder(top, clazz).build();

        var source = APContext.createSourceFile(top + "." + clazz.name(), beans.stream().map(BeanReader::element).toArray(Element[]::new));
        var writer = source.openWriter();
        file.writeTo(writer);
        writer.close();
        this.written = true;
        return true;
    }



}

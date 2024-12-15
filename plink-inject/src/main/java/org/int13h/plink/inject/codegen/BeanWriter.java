package org.int13h.plink.inject.codegen;

import com.palantir.javapoet.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BeanWriter {

    private final BeanReader reader;
    private final JavaFileObject fileObject;
    private final String qualifiedClassName;
    private final String className;

    private boolean written = false;

    public BeanWriter(BeanReader reader) throws IOException {
        this.reader = reader;
        this.className = reader.simpleName() + "$Def";
        this.qualifiedClassName = String.format("%s.%s", reader.packageName(), this.className);
        this.fileObject = APContext.createSourceFile(qualifiedClassName, reader.element());
    }

    public Optional<TypeName> typeName() {
        var type = Optional.ofNullable(APContext.elements().getTypeElement(this.qualifiedClassName)).map(TypeElement::asType);
        if(type.isPresent()) {
            if(type.get().getKind() == TypeKind.ERROR) {
                return Optional.empty();
            }
            return Optional.of(TypeName.get(type.get()));
        }
        return Optional.empty();
    }

    public boolean shouldDelay() {
        var ele = APContext.typeElement("org.int13h.plink.inject.Builder");
        if(ele == null) {
            return true;
        }
        return ele.asType().getKind() == TypeKind.ERROR;
    }


    public void writeIsBeanAbsent(MethodSpec.Builder builder) {
        List<Object> absentiaParams = new ArrayList<>();

        var absentia = new StringBuilder();
        absentia.append("if(builder.isBeanAbsent(");
        if(reader.name().isPresent()) {
            absentia.append("$S"); absentiaParams.add(reader.name().get());
        } else {
            absentia.append("null");
        }
        for(var string : reader.provides()) {
            absentia.append(", $T.class");
            absentiaParams.add(TypeName.get(string.asType()));
        }
        absentia.append("))");

        builder.beginControlFlow(absentia.toString(), absentiaParams.toArray());
    }

    public void writeBeanConstruction(MethodSpec.Builder builder) {
        var constructor = new StringBuilder();
        var params = new ArrayList<>();

        constructor.append("$T bean = new $T(");
        params.add(reader.typeName());
        params.add(reader.typeName());
        var typeList = reader.getInjectionTypes().stream().map(TypeName::get).toList();
        for(int i = 0; i < typeList.size(); i++) {
            //TODO: Improve for named beans.
            constructor.append("builder.get($T.class)");
            params.add(typeList.get(i));
            if(i < typeList.size() - 1) {
                constructor.append(", ");
            }
        }
        constructor.append(")");

        builder.addStatement(constructor.toString(), params.toArray());
    }

    public void writeRegister(MethodSpec.Builder builder) {
        var register = new StringBuilder();
        var params = new ArrayList<>();
        register.append("builder.register(");
        if(reader.name().isPresent()) {
            register.append("$S"); params.add(reader.name().get());
        } else {
            register.append("null");
        }

        register.append(", bean");

        var provides = reader.provides();
        for(int i = 0; i < provides.size(); i++) {
            register.append(", $T.class");
            params.add(TypeName.get(provides.get(i).asType()));
        }

        register.append(")");


        builder.addStatement(register.toString(), params.toArray());
    }


    public void write() throws IOException {
        if(this.written) return;




        List<TypeName> parTypes = reader.getInjectionTypes().stream().map(TypeName::get).collect(Collectors.toCollection(ArrayList::new));
        parTypes.addFirst(reader.typeName());

        var builder = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(APContext.typeElement("org.int13h.plink.inject.Builder").asType()), "builder");

        writeIsBeanAbsent(builder);
        writeBeanConstruction(builder);
        writeRegister(builder);

        builder.endControlFlow();

        TypeSpec clazz = TypeSpec.classBuilder(reader.simpleName() + "$Def")
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addMethod(builder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(reader.packageName(), clazz).build();

        var writer = fileObject.openWriter();
        javaFile.writeTo(writer);
        writer.close();
        this.written = true;
    }

    public void writeBeanMeta(AnnotationSpec.Builder annotation) {
        annotation.addMember("name", "$S", reader.type());
        for(var provides : reader.provides()) {
            annotation.addMember("provides", "$S", provides);
        }
        for(var dependsOn : reader.dependsOn()) {
            annotation.addMember("depends", "$S", dependsOn);
        }
        if(reader.isOptional()) {

        }
    }
}

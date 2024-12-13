package org.int13h.plink.codegen.inject;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BeanWriter {

    private final BeanReader reader;
    private final JavaFileObject fileObject;
    private final String qualifiedClassName;
    private final String className;

    private boolean written = false;

    public BeanWriter(BeanReader reader) throws IOException {
        this.reader = reader;
        this.className = reader.simpleName() + "$BeanDefinition";
        this.qualifiedClassName = String.format("%s.%s", reader.packageName(), this.className);
        this.fileObject = APContext.createSourceFile(qualifiedClassName, reader.element());
    }


    public void write() throws IOException {
        if(this.written) return;
        var parList = new StringBuilder();
        parList.append("return new $T(");
        for(int i = 0; i < reader.getInjectionTypes().size(); i++) {
            parList.append("builder.get($T.class)");
            if(i < reader.getInjectionTypes().size() - 1) {
                parList.append(", ");
            }
        }
        parList.append(")");

        List<TypeName> parTypes = reader.getInjectionTypes().stream().map(TypeName::get).collect(Collectors.toCollection(ArrayList::new));
        parTypes.addFirst(reader.typeName());

        MethodSpec spec = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(reader.typeName())
                .addParameter(TypeName.get(APContext.typeElement("org.int13h.plink.injection.Builder").asType()), "builder")
                .addStatement(parList.toString(), parTypes.toArray())
                .build();

        TypeSpec clazz = TypeSpec.classBuilder(reader.simpleName() + "$BeanDefinition")
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addMethod(spec)
                .build();

        JavaFile javaFile = JavaFile.builder(reader.packageName(), clazz).build();

        var writer = fileObject.openWriter();
        javaFile.writeTo(writer);
        writer.close();
        this.written = true;
    }
}

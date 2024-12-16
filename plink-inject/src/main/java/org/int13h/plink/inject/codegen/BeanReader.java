package org.int13h.plink.inject.codegen;

import com.palantir.javapoet.TypeName;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BeanReader {

    private TypeElement beanType;

    private ExecutableElement constructor;
    private ArrayList<TypeElement> provides = new ArrayList<>();

    private boolean optional = false;

    public BeanReader(TypeElement beanType) {
        this.beanType = beanType;
    }

    public List<String> dependsOn() {
        var constructor = this.constructor();
        return constructor.getParameters().stream().map(param -> APContext.asTypeElement(param.asType()).getQualifiedName().toString()).collect(Collectors.toList());
    }

    public List<String> provideNames() {
        if(provides.isEmpty()) {
            provides.add(beanType);
            classImplements(beanType, provides);
        }
        return provides.stream().map(e -> e.getQualifiedName().toString()).toList();
    }

    public List<TypeElement> provides() {
        if(provides.isEmpty()) {
            provides.add(beanType);
            classImplements(beanType, provides);
        }
        return provides;
    }

    public boolean isOptional() {
        return RequiresPropertyPrism.isPresent(this.beanType);
    }

    Optional<RequiresPropertyPrism> getRequiresProperty() {
        return RequiresPropertyPrism.getOptionalOn(this.beanType);
    }

    public Element element() {
        return beanType;
    }

    public boolean shouldDelay() {
        return constructor().getParameters().stream().map(Element::asType).anyMatch(e -> e.getKind() == TypeKind.ERROR);
    }

    public Optional<String> name() {
        return Optional.empty();
    }

    public String type() {
        return beanType.getQualifiedName().toString();
    }


    public String simpleName() {
        return beanType.getSimpleName().toString();
    }

    public TypeName typeName() {
        return TypeName.get(beanType.asType());
    }

    public String packageName() {
        return APContext.elements().getPackageOf(beanType).getQualifiedName().toString();
    }

    private void classImplements(TypeElement classElement, List<TypeElement> results) {
        if(classElement == null) return;

        var interfaces = classElement.getInterfaces().stream().map(APContext::asTypeElement).toList();
        var superClass = APContext.asTypeElement(classElement.getSuperclass());
        if(superClass != null && !superClass.getQualifiedName().contentEquals(Object.class.getName())) {
            results.add(superClass);
            classImplements(superClass, results);
        }

        for(var type : interfaces) {
            if(!results.contains(type)) {
                results.add(type);
                classImplements(type, results);
            }
        }
    }

    /**
     * Find the constructor to inject with.
     * @return
     */
    public ExecutableElement constructor() {
        if(this.constructor != null) {
            return this.constructor;
        }
        var constructors = this.beanType.getEnclosedElements().stream().filter(e -> e.getKind() == ElementKind.CONSTRUCTOR).toList();
        var injectAnnotated = constructors.stream().filter(InjectPrism::isPresent).toList();
        if(constructors.size() == 1) {
            this.constructor = (ExecutableElement) constructors.get(0);
        } else if(injectAnnotated.size() == 1) {
                this.constructor = (ExecutableElement) injectAnnotated.get(0);
        } else {
            throw new IllegalStateException("Failed to find Injectable constructor for class " + beanType);
        }
        return this.constructor;
    }

    public List<TypeMirror> getInjectionTypes() {
        if(constructor() != null) {
            var c = constructor();
            return c.getParameters().stream().map(VariableElement::asType).toList();
        }
        return List.of();
    }
}

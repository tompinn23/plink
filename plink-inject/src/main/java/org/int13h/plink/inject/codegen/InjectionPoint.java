package org.int13h.plink.inject.codegen;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.stream.Collectors;

public class InjectionPoint {

    private List<TypeElement> dependencies;
    private Element element;

    private InjectionPoint(Element element, List<TypeElement> dependencies) {
        this.element = element;
        this.dependencies = dependencies;
    }

    public static InjectionPoint from(ExecutableElement element) {
        return new InjectionPoint(
                element,
                element.getParameters().stream().map(el -> APContext.asTypeElement(el.asType())).toList());
    }


}

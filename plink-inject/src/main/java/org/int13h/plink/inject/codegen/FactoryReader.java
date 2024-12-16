package org.int13h.plink.inject.codegen;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FactoryReader {

    private final TypeElement factoryType;

    private final List<InjectionPoint> injectionPoints = new ArrayList<>();

    public FactoryReader(final TypeElement factoryType) {
        this.factoryType = factoryType;

        scanConstructor();
        scanBeans();
    }

    private void scanConstructor() {

    }

    private void scanBeans() {
        for(var el : factoryType.getEnclosedElements()) {
            if(el.getKind() == ElementKind.METHOD && BeanPrism.isPresent(el)) {
                var injection = InjectionPoint.from((ExecutableElement) el);
                injectionPoints.add(injection);
            }
        }
    }


    public class Writer {

    }

}

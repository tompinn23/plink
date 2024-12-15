package org.int13h.plink.inject.codegen;


import io.avaje.prism.GenerateAPContext;
import io.avaje.prism.GenerateUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.int13h.plink.inject.codegen.APContext.typeElement;

@GenerateUtils
@GenerateAPContext
@SupportedOptions("mergeServices")
@SupportedAnnotationTypes({
        SingletonPrism.PRISM_TYPE,
})
public class InjectProcessor extends AbstractProcessor {

    private final BeanScope singletons = new BeanScope();

    private final Set<BeanReader> delayedBeans = new HashSet<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        APContext.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        if(round.processingOver()) {
            APContext.clear();
            return false;
        }

        try {
            writeDelayedBeans();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var beans = maybeElements(round, SingletonPrism.PRISM_TYPE).map(this::readBeans).orElse(Set.of());
        beans.forEach(b -> {
            try {
                var writer = singletons.getBeanWriter(b);
                if(b.shouldDelay() || writer.shouldDelay()) {
                    delayedBeans.add(b);
                } else {
                    writer.write();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            singletons.write(round.processingOver());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private Set<BeanReader> readBeans(Set<? extends Element> set) {
        return readChangedBeans(ElementFilter.typesIn(set), false, false);
    }

    /**
     * Read the beans that have changed.
     */
    private Set<BeanReader> readChangedBeans(Set<TypeElement> beans, boolean factory, boolean importedComponent) {
        Set<BeanReader> changedBeans = new HashSet<>();
        for (final var typeElement : beans) {
            if (typeElement.getKind() == ElementKind.INTERFACE) {
                continue;
            }
            var reader = new BeanReader(typeElement);
            singletons.addBeanDefinition(reader);
            changedBeans.add(reader);
        }
        return changedBeans;
    }

    private void writeDelayedBeans() throws IOException {
        for(BeanReader reader : delayedBeans) {
            if(!reader.shouldDelay()) {
                new BeanWriter(reader).write();
                delayedBeans.remove(reader);
            }
        }
    }

    // Optional because these annotations are not guaranteed to exist
    private static Optional<? extends Set<? extends Element>> maybeElements(RoundEnvironment round, String name) {
        return Optional.ofNullable(typeElement(name)).map(round::getElementsAnnotatedWith);
    }
}

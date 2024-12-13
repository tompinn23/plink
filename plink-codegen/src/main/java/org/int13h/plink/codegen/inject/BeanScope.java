package org.int13h.plink.codegen.inject;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.*;

public class BeanScope {

    private final Map<String, BeanReader> beanNames;
    private final List<BeanReader> beans;

    private final Map<BeanReader, BeanWriter> beanWriters = new HashMap<>();

    public BeanScope() {
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
        for(String provides : beanReader.provides()) {
            if(beanNames.containsKey(provides)) {
                throw new IllegalStateException(String.format("Duplicate bean registration %s provides %s already", beanNames.get(provides).name(), provides));
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
}

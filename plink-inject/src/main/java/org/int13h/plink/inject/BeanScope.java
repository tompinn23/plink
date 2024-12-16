package org.int13h.plink.inject;

import org.int13h.plink.inject.def.DefaultBeanScope;

import java.lang.reflect.Type;

public interface BeanScope {

    <T> T get(Type type);

    static BeanScope load() {
        return DefaultBeanScope.load(Thread.currentThread().getContextClassLoader());
    }
}

package org.int13h.plink.inject;

import java.lang.reflect.Type;

public interface Builder {

    boolean isBeanAbsent(String name, Type... types);

    <T> boolean register(String name, T instance, Type... types);

    <T> boolean register(T instance);

    <T> T get(Type clazz);

    boolean hasProperty(String name);
    String getProperty(String name);
}

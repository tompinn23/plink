package org.int13h.plink.inject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.ServiceLoader;

public class Beans implements Builder {

    private final HashMap<String, BeanEntry<?>> beanMap = new HashMap<>();

    private final ConfigService configService;

    public Beans(final ConfigService configService) {
        this.configService = configService;
    }

    private Type firstOf(Type[] array) {
        return array.length == 0 ? null : array[0];
    }

    @Override
    public boolean isBeanAbsent(String name, Type... types) {
        Type inject = firstOf(types);
        if(inject == null) {
            throw new IllegalArgumentException("Bean has no injection type");
        }
        if(name != null) {
            return !beanMap.containsKey(name);
        }
        return !beanMap.containsKey(inject.getTypeName());
    }

    @Override
    public <T> boolean register(String name, T instance, Type... types) {
        var inject = firstOf(types);
        if(inject == null) {
            throw new IllegalArgumentException("Bean has no injection type");
        }
        var entry = new BeanEntry<>(instance);
        if(name != null) {
            beanMap.put(name, entry);
        }
        for(Type type : types) {
            if(!beanMap.containsKey(type.getTypeName())) {
                beanMap.put(type.getTypeName(), entry);
            }
        }
        return true;
    }

    @Override
    public <T> boolean register(T instance) {
        return register(null, instance, instance.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Type clazz) {
        var entry = beanMap.get(clazz.getTypeName());
        if(entry != null) {
            return (T) entry.get();
        }
        return null;
    }

    public boolean hasProperty(String name) {
        return configService.get(name) != null;
    }

    public String getProperty(String name) {
        return configService.get(name);
    }


    public static Beans load(ClassLoader classLoader) {
        var loader = ServiceLoader.load(InjectService.class, classLoader);
        var container = loader.stream().filter(i -> i.type() == BeanContainer.class).map(svc -> (BeanContainer)svc.get()).findFirst();
        var config = loader.stream().filter(i -> i.type() == ConfigService.class).findFirst().map(svc -> (ConfigService)svc.get()).orElse(new SystemPropertyConfig());
        var scope = new Beans(config);
        container.ifPresent(beanContainer -> beanContainer.build(scope));
        return scope;
    }

    private static class BeanEntry<T> {
        T object;

        public BeanEntry(T object) {
            this.object = object;
        }

        public T get() {
            return object;
        }
    }
}

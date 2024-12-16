package org.int13h.plink.inject.def;

import jakarta.inject.Provider;
import org.int13h.plink.inject.BeanEntry;
import org.int13h.plink.inject.BeanScope;
import org.int13h.plink.inject.Builder;
import org.int13h.plink.inject.SystemPropertyConfig;
import org.int13h.plink.inject.spi.BeanContainer;
import org.int13h.plink.inject.spi.ConfigService;
import org.int13h.plink.inject.spi.InjectService;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;

public class DefaultBeanScope implements BeanScope, Builder {


    private final HashMap<String, BeanContextEntry> beanMap = new HashMap<>();

    private final ConfigService configService;

    public DefaultBeanScope(final ConfigService configService) {
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
        beanMap.computeIfAbsent(name, (k) -> new BeanContextEntry()).add(BeanContextEntry.entry(instance, name, BeanEntry.NORMAL));
        var entry = beanMap.get(name);
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
            return (T) entry.get(null);
        }
        return null;
    }

    public boolean hasProperty(String name) {
        return configService.get(name) != null;
    }

    public String getProperty(String name) {
        return configService.get(name);
    }


    public static DefaultBeanScope load(ClassLoader classLoader) {
        var loader = ServiceLoader.load(InjectService.class, classLoader);
        var container = loader.stream().filter(i -> BeanContainer.class.isAssignableFrom(i.type())).map(svc -> (BeanContainer)svc.get()).findFirst();
        var config = loader.stream().filter(i -> ConfigService.class.isAssignableFrom(i.type())).findFirst().map(svc -> (ConfigService)svc.get()).orElse(new SystemPropertyConfig());
        var scope = new DefaultBeanScope(config);
        container.ifPresent(beanContainer -> beanContainer.build(scope));
        return scope;
    }

    private static class BeanContextEntry {

        private final List<Entry> entries = new ArrayList<>(5);

        @Override
        public String toString() {
            return String.valueOf(entries);
        }

        List<Entry> entries() {
            return entries;
        }

        void add(Entry entry) {
            entries.add(entry);
        }

        Object get(String name) {
            if(entries.size() == 1) {
                return entries.get(0).bean();
            }
            return new Matcher(name).match(entries);
        }

        /**
         * Matching rules.
         * ~dependency
         */
        private static class Matcher {
            private final String name;
            private final boolean implicit;

            private Entry match;
            private Entry ignoredSecondaryMatch;


            public Matcher(String name) {
                if (name != null && name.startsWith("!")) {
                    this.name = name.substring(1);
                    this.implicit = true;
                } else {
                    this.name = name;
                    this.implicit = false;
                }
            }

            private Object match(List<Entry> entries) {
                Entry foundMatch = findMatch(entries);
                return foundMatch == null ? null : foundMatch.bean();
            }

            private Entry findMatch(List<Entry> entries) {
                for (Entry entry : entries) {
                    if (entry.isNameEqual(name)) {
                        checkMatch(entry);
                    }
                }
                if (match == null && implicit) {
                    // match without implied name, name = null to match against beans with no qualifier
                    for (var entry : entries) {
                        if (entry.isNameEqual(null)) {
                            checkMatch(entry);
                        }
                    }
                }
                if (match == null && (name == null || implicit)) {
                    // match no qualifier injection point to any beans
                    for (var entry : entries) {
                        checkMatch(entry);
                    }
                }
                return candidate();
            }

            private void checkMatch(Entry entry) {
                if (match == null) {
                    match = entry;
                    return;
                }
                if (match.isSecondary() && !entry.isSecondary()) {
                    // secondary loses
                    match = entry;
                    return;
                }
                if (match.isPrimary()) {
                    if (entry.isPrimary()) {
                        throw new IllegalStateException("Expecting only 1 bean match but have multiple primary beans " + match.bean() + " and " + entry.bean());
                    }
                    // leave as is, current primary wins
                    return;
                }
                if (entry.isSecondary()) {
                    if (match.isSecondary()) {
                        ignoredSecondaryMatch = entry;
                    }
                    return;
                }
                if (entry.isPrimary()) {
                    // new primary wins
                    match = entry;
                    return;
                }
                // try to resolve match using qualifier name (including null)
                if (match.isNameEqual(name) && !entry.isNameEqual(name)) {
                    ignoredSecondaryMatch = entry;
                    return;
                } else if (!match.isNameEqual(name) && entry.isNameEqual(name)) {
                    match = entry;
                    return;
                }
                throw new IllegalStateException("Expecting only 1 bean match but have multiple matching beans " + match.bean()
                        + " and " + entry.bean() + ". Maybe need a rebuild is required after adding a @Named qualifier?");
            }

            private Entry candidate() {
                if (match == null) {
                    return null;
                }
                checkSecondary();
                return match;
            }

            private void checkSecondary() {
                if (match.isSecondary() && ignoredSecondaryMatch != null) {
                    throw new IllegalStateException("Expecting only 1 bean match but have multiple secondary beans " + match.bean() + " and " + ignoredSecondaryMatch.bean());
                }
            }

        }

        static Entry entry(Object instance, String name, int flag) {
            return new Entry(name, instance, flag);
        }

        public static class Entry {

            protected final Object instance;
            protected final String name;
            protected final int flag;

            public Entry(String name, Object instance, int flag) {
                this.name = name;
                this.instance = instance;
                this.flag = flag;
            }

            public String name() {
                return name;
            }

            public Object bean() {
                return instance;
            }

            public int priority() {
                return flag;
            }

            final boolean isPrimary() {
                return flag == BeanEntry.PRIMARY;
            }

            final boolean isSecondary() {
                return flag == BeanEntry.SECONDARY;
            }

            final boolean isNameMatch(String qualifier) {
                return qualifier == null || qualifier.equalsIgnoreCase(name);
            }

            final boolean isNameEqual(String qualifier) {
                return qualifier == null ? name == null : qualifier.equalsIgnoreCase(name);
            }
        }
    }
}

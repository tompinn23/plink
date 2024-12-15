package org.int13h.plink.inject;

public class BeanDefinition {

    public final String[] provide;
    public final String[] depends;

    public final String name;

    private BeanDefinition(String name, String[] provide, String[] depends) {
        this.name = name;
        this.provide = provide;
        this.depends = depends;
    }

    public String name() {
        return name;
    }

    public String[] provides() {
        return provide;
    }

    public String[] depends() {
        return depends;
    }

    public static BeanDefinition fromBeanMeta(BeanMeta meta) {
        if(meta == null) return null;
        return new BeanDefinition(meta.name(), meta.provides(), meta.depends());
    }


}

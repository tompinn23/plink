package org.int13h.plink.inject;

public interface BeanContainer extends InjectService {

    Class<?>[] provides();

    Class<?>[] classes();

    void build(Builder builder);

}

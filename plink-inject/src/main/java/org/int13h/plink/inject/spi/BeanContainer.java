package org.int13h.plink.inject.spi;

import org.int13h.plink.inject.Builder;

public interface BeanContainer extends InjectService {

    Class<?>[] provides();

    Class<?>[] classes();

    void build(Builder builder);

}

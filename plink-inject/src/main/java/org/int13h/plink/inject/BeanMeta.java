package org.int13h.plink.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BeanMeta {
    String name();
    String[] provides();
    String[] depends() default {};
}

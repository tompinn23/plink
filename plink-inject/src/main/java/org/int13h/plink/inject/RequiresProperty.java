package org.int13h.plink.inject;

public @interface RequiresProperty {
    String property() default "";
    String value() default "";
}

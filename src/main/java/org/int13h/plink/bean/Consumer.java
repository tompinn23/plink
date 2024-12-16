package org.int13h.plink.bean;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.int13h.plink.inject.RequiresProperty;

@Singleton
public class Consumer implements BeanInterface {

    @Inject
    public Consumer(Bean bean, Dependency dependency) {}

    public Consumer() {}
}

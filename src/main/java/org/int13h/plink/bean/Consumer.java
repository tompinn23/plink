package org.int13h.plink.bean;

import jakarta.inject.Singleton;

@Singleton
public class Consumer implements BeanInterface {

    public Consumer(Bean bean, Dependency dependency) {}
}

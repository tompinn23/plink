package org.int13h.plankton.bean;

import jakarta.inject.Singleton;

@Singleton
public class Consumer implements BeanInterface {

    public Consumer(Bean bean, Dependency dependency) {}
}

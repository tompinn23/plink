package org.int13h.plink.bean;

import org.int13h.plink.inject.Bean;
import org.int13h.plink.inject.Factory;

@Factory
public class BeanFactory {

    @Bean
    public Consumer consume3() {
        return new Consumer();
    }
}

package org.int13h.plink.bean;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.int13h.plink.server.HttpRequest;
import org.int13h.plink.server.HttpResponse;

@Singleton
public class Bean {

    public Bean(HttpResponse dep) {

    }

    @Inject
    public Bean() {}
}

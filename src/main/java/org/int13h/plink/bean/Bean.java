package org.int13h.plink.bean;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.int13h.plink.inject.RequiresProperty;
import org.int13h.plink.server.HttpResponse;

@Singleton
@RequiresProperty(property = "yum.prop", value = "true")
public class Bean {

    public Bean(HttpResponse dep) {

    }

    @Inject
    public Bean() {}
}

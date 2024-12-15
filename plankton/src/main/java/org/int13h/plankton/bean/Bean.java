package org.int13h.plankton.bean;

import io.avaje.inject.RequiresProperty;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@RequiresProperty("use.service")
public class Bean {


    @Inject
    public Bean() {}
}

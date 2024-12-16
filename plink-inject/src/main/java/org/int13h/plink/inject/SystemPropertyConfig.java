package org.int13h.plink.inject;

import org.int13h.plink.inject.spi.ConfigService;

public class SystemPropertyConfig implements ConfigService {

    @Override
    public String get(String property) {
        return System.getProperty(property);
    }
}

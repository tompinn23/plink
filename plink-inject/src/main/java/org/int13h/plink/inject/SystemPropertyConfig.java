package org.int13h.plink.inject;

public class SystemPropertyConfig implements ConfigService {

    @Override
    public String get(String property) {
        return System.getProperty(property);
    }
}

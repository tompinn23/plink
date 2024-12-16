package org.int13h.plink.inject.spi;

public interface ConfigService extends InjectService {
    String get(String property);
}

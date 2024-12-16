package org.int13h.plink.inject;

import java.util.Set;

public interface BeanEntry {

    int PRIMARY = 1;
    int NORMAL = 0;
    int SECONDARY = -1;

    String qualifier();
    Object bean();
    int priority();
    Class<?> type();

    Set<String> keys();
    boolean hasKey(Class<?> key);
}

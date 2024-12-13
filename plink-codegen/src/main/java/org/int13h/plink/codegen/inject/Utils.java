package org.int13h.plink.codegen.inject;

public class Utils {

    public static String commonPackage(String top, String pkg) {
        if(pkg == null) return top;
        if(top == null) return pkg;
        if(pkg.startsWith(top)) {
            return top;
        }

        int next;
        do {
            next = top.lastIndexOf('.');
            if(next > -1) {
                top = top.substring(0, next);
                if(pkg.startsWith(top)) {
                    return top;
                }
            }
        } while(next > -1);
        return top;
    }
}

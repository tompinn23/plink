package org.int13h.plink.server;

import org.int13h.plink.router.HttpMethod;

public interface HttpRequest {

    HttpMethod method();

    String path();

    String param(String name);
}

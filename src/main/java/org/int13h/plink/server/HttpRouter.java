package org.int13h.plink.server;

import org.int13h.plink.router.Router;

import java.util.function.BiConsumer;

public class HttpRouter extends Router<BiConsumer<HttpRequest, HttpResponse>> {

    public HttpRouter(BiConsumer<HttpRequest, HttpResponse> h404, BiConsumer<HttpRequest, HttpResponse> h405) {
        super(h404, h405);
    }
}

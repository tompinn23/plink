package org.int13h.plink.server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.int13h.plink.router.HttpMethod;
import org.int13h.plink.router.Route;
import org.int13h.plink.router.RouteHandler;

import java.util.Map;


public class NettyHttpRequest implements HttpRequest {

    private HttpMethod method = null;

    private final FullHttpRequest request;
    private final QueryStringDecoder queryStringDecoder;

    private RouteHandler<?> handler;
    private Map<String, String> pathParameters;

    public NettyHttpRequest(FullHttpRequest request) {
        this.request = request;
        this.queryStringDecoder= new QueryStringDecoder(request.uri());
    }

    void setHandler(RouteHandler<?> handler) {
        this.handler = handler;
        this.pathParameters = NettyUtil.paramMap(path(), this.handler);
    }

    public HttpMethod method() {
        if(this.method != null) {
            return this.method;
        }
        this.method = NettyUtil.convert(request.method());
        return this.method;
    }

    @Override
    public String path() {
        return queryStringDecoder.path();
    }

    @Override
    public String param(String name) {
        return pathParameters.get(name);
    }
}

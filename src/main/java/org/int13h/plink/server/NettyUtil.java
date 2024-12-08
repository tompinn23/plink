package org.int13h.plink.server;

import org.int13h.plink.router.HttpMethod;
import org.int13h.plink.router.RouteHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NettyUtil {

    public static HttpMethod convert(io.netty.handler.codec.http.HttpMethod method) {
        return switch (method.toString()) {
            case "GET" -> HttpMethod.GET;
            case "POST" -> HttpMethod.POST;
            case "PUT" -> HttpMethod.PUT;
            case "DELETE" -> HttpMethod.DELETE;
            case "HEAD" -> HttpMethod.HEAD;
            case "OPTIONS" -> HttpMethod.OPTIONS;
            default -> HttpMethod.NOT_ALLOWED;
        };
    }

    public static Map<String, String> paramMap(String uri, RouteHandler<?> handler) {
        if(handler.params() == null || handler.params().isEmpty()) {
            return Map.of();
        }
        String[] parts = uri.split("/");
        return handler.params()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> parts[entry.getValue()]
        ));
    }
}

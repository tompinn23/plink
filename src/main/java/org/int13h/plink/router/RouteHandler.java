package org.int13h.plink.router;

import java.util.Map;

public record RouteHandler<T>(Map<String, Integer> params, T handler) {
}
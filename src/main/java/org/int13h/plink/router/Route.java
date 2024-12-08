package org.int13h.plink.router;

public record Route<T>(Node<T> node, RouteHandler<T> handler, int wildcardLen) {
}

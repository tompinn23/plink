package org.int13h.plink.router;

public class Router<T> extends Group<T> {

    private final Node<T> tree;

    private final RouteHandler<T> handle_404;
    private final RouteHandler<T> handle_405;

    public Router(T h404, T h405) {
        super("");
        tree = new Node<>("/");

        this.handle_404 = new RouteHandler<>(null, h404);
        this.handle_405 = new RouteHandler<>(null, h405);
    }

    public Node<T> getTree() {
        return tree;
    }

    @Override
    Router<T> getRouter() {
        return this;
    }

    public RouteHandler<T> handler(HttpMethod method, String route) {
        var result = this.tree.find(method, route);
        if (result == null || (result.node() == null && result.handler() == null)) {
            return handle_404;
        }
        if(result.node() != null && result.handler() == null) {
            return handle_405;
        }
        return result.handler();
    }
}

package org.int13h.plink.router;

import java.util.Map;


public abstract class Group<T> {

    private final String path;

    protected Group(final String path) {
        this.path = path;
    }

    public Group<T> create(final String path) {
        return new RouterGroup<>(getRouter(), joinPath(this.path, path));
    }

    abstract Router<T> getRouter();

    public void handle(HttpMethod method, String path, T handler) {
        checkPath(path);
        path = this.path + path;
        if(path.isEmpty()) {
            throw new IllegalArgumentException("Empty path");
        }

        Pair<Node<T>, Map<String, Integer>> nodeParams = getRouter().getTree().add(path);
        var node = nodeParams.a();
        Map<String, Integer> params = nodeParams.b();

        node.setHandler(method, path, new RouteHandler<>(params, handler));
    }

    public Group<T> GET(final String path, T handler) {
        this.handle(HttpMethod.GET, path, handler);
        return this;
    }

    public Group<T> POST(final String path, T handler) {
        this.handle(HttpMethod.POST, path, handler);
        return this;
    }

    public Group<T> PUT(final String path, T handler) {
        this.handle(HttpMethod.PUT, path, handler);
        return this;
    }

    private static String joinPath(String base, String path) {
        checkPath(path);
        path = base + path;

        // Don't want trailing slash if path length is greater than 1
        if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    private static void checkPath(String path) {
        // All non-empty paths must start with a slash
        if (path != null && !path.isEmpty() && path.charAt(0) != '/') {
            throw new IllegalArgumentException(String.format("Path %s must start with a slash", path));
        }
    }

    public static class RouterGroup<T> extends Group<T> {
        private final Router<T> router;

        public RouterGroup(final Router<T> router, final String path) {
            super(path);
            this.router = router;
        }

        @Override
        Router<T> getRouter() {
            return this.router;
        }
    }

}



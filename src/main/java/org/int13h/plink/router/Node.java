package org.int13h.plink.router;

import java.util.*;

public class Node<T> {

    private String route = "";
    private String part;

    private final Map<HttpMethod, RouteHandler<T>> handlers = new EnumMap<>(HttpMethod.class);

    private Node<T> parent = null;
    private Node<T> colon = null;

    private boolean isWild = false;

    private final ArrayList<Node<T>> nodes = new ArrayList<>();

    private short[] table;
    private char minChar;
    private char maxChar;

    public Node(String part) {
        this.part = part;
    }

    public Node(String part, Node<T>[] nodes) {
        this.part = part;
        this.nodes.addAll(Arrays.asList(nodes));
    }

    public Pair<Node<T>, Map<String, Integer>> add(String route) {
        var pc = PathContainer.parse(route);

        var current = this;

        for(var part : pc.parts()) {
            current = current.addPart(part);
        }
        if(current.route.isEmpty()) {
            current.route = route;
        }
        this.indexNodes();

        return new Pair<>(current, pc.params());
    }

    public Node<T> addPart(String part) {
        if(part.equals("*")) {
            this.isWild = true;
            return this;
        }

        if(part.equals(":")) {
            if(this.colon == null) {
                this.colon = new Node<>(":");
            }
            return this.colon;
        }

        for(int i = 0; i < this.nodes.size(); i++) {
            var node = this.nodes.get(i);

            if(node.part.charAt(0) != part.charAt(0)) {
                continue;
            }

            for(int j = 0; j < part.length(); j++) {
                var c = part.charAt(j);
                if(j == node.part.length()) {
                    break;
                }

                if(c == node.part.charAt(j)){
                    continue;
                }

                node.part = node.part.substring(j);
                var newNode = new Node<T>(node.part.substring(j));

                nodes.add(i, new Node<T>(part.substring(0, j), new Node[]{node, newNode}));

                return newNode;
            }
            if((part.length() > node.part.length())) {
                part = part.substring(node.part.length());
                return node.addPart(part);
            } else if(part.length() < node.part.length()) {
                node.part = node.part.substring(part.length());
                var newNode = new Node<T>(part, new Node[]{node});
                nodes.add(i, newNode);
                return newNode;
            } else {
                return node;
            }
        }

        var node = new Node<T>(part);
        nodes.add(node);
        return node;
    }

    private void indexNodes() {
        if(!this.nodes.isEmpty()) {
            this._indexNodes();
        }

        if(this.colon != null) {
            this.colon.parent = this;
            this.colon.indexNodes();
        }
    }

    private void _indexNodes() {
        this.nodes.sort(Comparator.comparingInt(o -> o.part.charAt(0)));

        this.minChar = this.nodes.getFirst().part.charAt(0);
        this.maxChar = this.nodes.getLast().part.charAt(0);

        int size = this.maxChar - this.minChar + 1;
        if(this.table == null || this.table.length != size) {
            this.table = new short[size];
        } else {
            Arrays.fill(this.table, (short) 0);
        }

        for(int i = 0; i < this.nodes.size(); i++) {
            var node = this.nodes.get(i);
            node.parent = this;
            node.indexNodes();

            var firstChar = node.part.charAt(0) - this.minChar;

            this.table[firstChar] = (short) (i + 1);
        }
    }

    public Route<T> find(HttpMethod method, String route) {
        if(route.isEmpty()) {
            return null;
        }
        route = route.substring(1);
        if(route.isEmpty()) {
            if (handlers.containsKey(method)) {
                return new Route<>(this, handlers.get(method), 0);
            }
            return null;
        }

        return _find(method, route);
    }

    private Route<T> _find(HttpMethod method, String path) {
        Node<T> found = null;

        char first = path.charAt(0);
        if(first >= minChar && first <= maxChar) {
            int i = table[first - this.minChar];
            if(i != 0) {
                var child = nodes.get(i - 1);
                if(child.part.equals(path)) {
                    if(child.handlers.containsKey(method)) {
                        return new Route<>(child, child.handlers.get(method), 0);
                    }
                    found = child;
                } else {
                    var partLength = child.part.length();
                    if(path.startsWith(child.part)) {
                        Route<T> result = child._find(method, path.substring(partLength));
                        if(result.handler() != null) {
                            return result;
                        }
                        if(result.node() != null) {
                            found = result.node();
                        }
                    }
                }
            }
        }

        if(colon != null) {
            int i = path.indexOf('/');
            if(i > 0) {
                var result = colon._find(method, path.substring(i));
                if(result.handler() != null) {
                    return result;
                }
            } else {
                if(colon.handlers.containsKey(method)) {
                    return new Route<>(colon, colon.handlers.get(method), 0);
                }
                if(found == null) {
                    found = colon;
                }
            }
        }

        if(isWild) {
            if(handlers.containsKey(method)) {
                return new Route<>(this, handlers.get(method), path.length());
            }
            if(found == null) {
                found = this;
            }
        }

        return new Route<>(found, null, 0);
    }

    public void setHandler(HttpMethod method, String path, RouteHandler<T> handler) {
        if(this.handlers.containsKey(method)) {
            if(route.equals(path)) {
                throw new IllegalArgumentException(String.format("Route %s already handles %s", route, method));
            }
            throw new IllegalStateException(String.format("Route %s and %s can't both handle %s", route, path, method));
        }
        this.handlers.put(method, handler);
    }

}



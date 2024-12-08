package org.int13h.plink.router;

import java.nio.file.Path;
import java.util.*;

public class PathContainer {

    private String[] parts;
    private Map<String, Integer> params;

    public String[] parts() {
        return parts;
    }

    public Map<String, Integer> params() {
        return params;
    }

    private PathContainer() {
        this.parts = new String[0];
        this.params = new HashMap<>();
    }

    private PathContainer(final String[] parts, final Map<String, Integer> params) {
        this.parts = parts;
        this.params = params;
    }

    public static PathContainer parse(String path) {
        if(!path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with '/'");
        }

        if(path.equals("/")) {
            return new PathContainer();
        }

        path = path.substring(1);

        String[] parts = path.split("/");
        if(parts.length == 0) {
            throw new IllegalArgumentException("Invalid route: " + path);
        }

        var parser = new RouteParser(parts);

        var params = new ArrayList<String>();
        while(parser.valid()) {
            var segment = parser.next();
            if(segment.isEmpty()) {
                parser.accumulate("");
                continue;
            }

            switch (segment.substring(0, 1)) {
                case ":":
                    parser.finalize(true);
                    parser.addPart(":");
                    params.add(segment.substring(1));
                    break;
                case "*":
                    parser.finalize(true);
                    parser.addPart("*");
                    params.add(segment.substring(1));
                    break;
                default:
                    parser.accumulate(segment);
            }
        }

        parser.finalize(false);

        return new PathContainer(parser.parts.toArray(new String[0]), paramMap(path, parts, params));
    }

    private static Map<String, Integer> paramMap(String route, String[] parts, List<String> params) {
        Map<String, Integer> map = new HashMap<>(params.size());

        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);

            if (param == null || param.isEmpty()) {
                throw new IllegalArgumentException(String.format("Param must have a name: \"%s\"", route));
            }

            boolean found = false;

            int idx = 0;
            for(var p : parts) {
                if(p.startsWith(":") || p.startsWith("*")) {
                    if(p.substring(1).equals(param)) {
                        map.put(param, idx);
                        found = true;
                    }
                }
                idx++;
            }
            if(!found) {
                throw new IllegalArgumentException(String.format("Param \"%s\" not found in route %s", param, route));
            }
        }

        return map;
    }

    private static class RouteParser {

        private String[] segments;

        private int idx = 0;

        private ArrayList<String> accumulator = new ArrayList<>();
        private ArrayList<String> parts = new ArrayList<>();

        public RouteParser(String[] parts) {
            this.segments = parts;
        }

        public boolean valid() {
            return this.idx < segments.length;
        }

        public String next() {
            return segments[idx++];
        }

        public void accumulate(String part) {
            accumulator.add(part);
        }

        public void finalize(boolean slash) {
            var part = join(this.accumulator, slash);
            if(!part.isEmpty()) {
                parts.add(part);
            }
            accumulator.clear();

            if(valid()) {
                accumulator.add("");
            }
        }

        public void addPart(String part) {
            parts.add(part);
        }

        private static String join(List<String> parts, boolean slash) {
            if(parts == null || parts.isEmpty())
                return "";

            StringJoiner joiner = new StringJoiner("/");
            parts.forEach(joiner::add);
            var result = joiner.toString();

            if(slash) {
                return joiner + "/";
            }
            return result;
        }
    }
}

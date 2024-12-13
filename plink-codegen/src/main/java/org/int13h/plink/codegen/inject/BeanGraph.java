package org.int13h.plink.codegen.inject;

import java.util.*;
import java.util.stream.Collectors;

public class BeanGraph {

    private final List<BeanReader> sorted;
    private final BeanScope scope;

    public BeanGraph(BeanScope scope) {
        this.scope = scope;
        this.sorted = orderBeans();
    }

    private List<BeanReader> orderBeans() {
        Map<BeanReader, List<String>> adjacency = new HashMap<>();
        Map<BeanReader, Integer> inDegree = new HashMap<>();

        for(BeanReader bean : scope.getBeans()) {
            adjacency.putIfAbsent(bean, new ArrayList<>());
            inDegree.putIfAbsent(bean, 0);
        }

        for(BeanReader bean : scope.getBeans()) {
            for(var dependency : bean.dependsOn()) {
                adjacency.get(bean).add(dependency);
                inDegree.put(bean, inDegree.get(bean) + 1);
            }
        }

        Set<BeanReader> visited = new HashSet<>();
        Set<BeanReader> stack = new HashSet<>();
        List<BeanReader> sorted = new ArrayList<>();

        for(BeanReader bean : scope.getBeans()) {
            if(detectCycle(bean, adjacency, visited, stack, sorted)) {
                throw new IllegalArgumentException("Bean scope has circular dependency between " + stack);
            }
        }

//        Queue<BeanReader> queue = new LinkedList<>();
//        inDegree.entrySet()
//                .stream()
//                .filter(e -> e.getValue() == 0)
//                .map(Map.Entry::getKey)
//                .forEach(queue::add);
//
//        var sorted = new ArrayList<BeanReader>();
//        while(!queue.isEmpty()) {
//            BeanReader bean = queue.poll();
//            sorted.add(bean);
//
//            for(String dependent : adjacency.get(bean)) {
//                BeanReader neighbour = scope.getByClass(dependent).orElseThrow(() -> new IllegalStateException("No bean in bean scope " + scope + " provides " + dependent));
//                inDegree.put(neighbour, inDegree.get(neighbour) - 1);
//                if(inDegree.get(neighbour) == 0) {
//                    queue.add(neighbour);
//                }
//            }
//        }

        if(sorted.size() != scope.getBeans().size()) {
            throw new IllegalArgumentException("Bean Scope contained circular dependencies");
        }

        return sorted;
    }

    public List<BeanReader> getBeanOrder() {
        return sorted;
    }

    private boolean detectCycle(BeanReader bean, Map<BeanReader, List<String>> adjacency, Set<BeanReader> visited, Set<BeanReader> stack, List<BeanReader> sorted) {
        if(stack.contains(bean)) {
            return true;
        }
        if(visited.contains(bean)) {
            return false;
        }

        visited.add(bean);
        stack.add(bean);

        for(String dependent : adjacency.get(bean)) {
            BeanReader neighbour = scope.getByClass(dependent).orElseThrow(() -> new IllegalArgumentException("Bean of class " + dependent + " not found in scope"));
            if(detectCycle(neighbour, adjacency, visited, stack, sorted)) {
                return true;
            }
        }

        stack.remove(bean);
        sorted.add(bean);
        return false;
    }
}

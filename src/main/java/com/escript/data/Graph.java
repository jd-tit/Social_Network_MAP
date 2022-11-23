package com.escript.data;

import com.escript.exceptions.DuplicateElementException;
import com.escript.exceptions.EdgeNotFoundException;
import com.escript.exceptions.ID_NotFoundException;

import java.util.*;

public class Graph <ID_type> {
    HashMap<ID_type, HashSet<ID_type>> adjacencyList;
    HashMap<ID_type, Node> nodes;

    private class Node {
        ID_type id;
        boolean visited; //TODO: move all usage of the `visited` property to external containers

        Node(ID_type id){
            this.id = id;
            visited = false;
        }
    }

    public Graph(){
        this.adjacencyList = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public void addNode(ID_type id) throws DuplicateElementException {
        if(adjacencyList.containsKey(id))
            throw new DuplicateElementException(
                    "Node with ID \"%s\" already exists.".formatted(id.toString()));
        adjacencyList.put(id, new HashSet<>());
        nodes.put(id, new Node(id));
    }

    public void addEdge(ID_type id1, ID_type id2) throws DuplicateElementException, ID_NotFoundException {
        if(!nodes.containsKey(id1))
            throw new ID_NotFoundException(
                    "Node with ID \"%s\" does not exist.".formatted(id1.toString()));

        if(!nodes.containsKey(id2))
            throw new ID_NotFoundException(
                    "Node with ID \"%s\" does not exist.".formatted(id2.toString())
            );


        var list1 = adjacencyList.get(id1);
        var list2 = adjacencyList.get(id2);

        if(list1.contains(id2) || list2.contains(id1)){
            throw new DuplicateElementException(
                    "Edge with node IDs \"%s\" and \"%s\" already exists."
                            .formatted(id1.toString(), id2.toString())
            );
        }

        list1.add(id2);
        list2.add(id1);
    }

    public void removeNode(ID_type id) throws ID_NotFoundException {
        if(!adjacencyList.containsKey(id)){
            throw new ID_NotFoundException(
                    "Could not delete node with non-existent id of %s".formatted(id.toString()));
        }

        ArrayList<ID_type> adjacentNodes = new ArrayList<>(adjacencyList.get(id));

        //remove node to be deleted from the other node's adjacency lists
        for(var node_id : adjacentNodes){
            adjacencyList.get(node_id).remove(id);
        }
        adjacencyList.remove(id);
        nodes.remove(id);
    }

    public void removeEdge(ID_type id1, ID_type id2) throws ID_NotFoundException, EdgeNotFoundException {
        var list1 = adjacencyList.get(id1);
        var list2 = adjacencyList.get(id2);

        if(!hasNode(id1) || !hasNode(id2)){
            throw new ID_NotFoundException(
                    "Could not delete edge with non-existent node IDs");
        }

        if(!list1.contains(id2) || !list2.contains(id1)){
            throw new EdgeNotFoundException("Could not delete a non-existent edge");
        }

        list1.remove(id2);
        list2.remove(id1);
    }

    public int countConnectedComponents(){
        int count = 0;
        nodes.values().forEach(x -> x.visited = false);

        for(Node n : nodes.values()) {
            if(!n.visited) {
                DFS(n);
                count++;
            }
        }
        return count;
    }

    public ArrayList<ArrayList<ID_type>> getConnectedComponents(){
        nodes.values().forEach(x -> x.visited = false);
        ArrayList<ArrayList<ID_type>> communities = new ArrayList<>();
        for(Node n : nodes.values()) {
            if(!n.visited) {
                var community = getCommunityOf(n.id);
                communities.add(new ArrayList<>(community));
            }
        }
        return communities;
    }

    private void exploreComponent(Node start, Collection<ID_type> collector){
        start.visited = true;
        collector.add(start.id);
        for(ID_type id : adjacencyList.get(start.id)) {
            Node neighbour = nodes.get(id);
            if(!neighbour.visited)
                exploreComponent(neighbour, collector);
        }
    }

    public Collection<ID_type> getCommunityOf(ID_type id){
        LinkedList<ID_type> result = new LinkedList<>();
        exploreComponent(nodes.get(id), result);
        return result;
    }

    private void DFS(Node start){
        start.visited = true;
        for(ID_type id : adjacencyList.get(start.id)) {
            Node neighbour = nodes.get(id);
            if(!neighbour.visited)
                DFS(neighbour);
        }
    }

    private void longestPathInComponentRecursive(ID_type start, LinkedList<ID_type> longestPath,
                                        LinkedList<ID_type> currentPath, HashMap<ID_type, Boolean> visited){
        visited.put(start, true);

        if(currentPath.size() > longestPath.size()){
            longestPath.clear();
            longestPath.addAll(currentPath);
        }

        var neighbours = adjacencyList.get(start).parallelStream().sorted().toList();
        for(ID_type n_id : neighbours){
            if(!visited.get(n_id)) {
                currentPath.addLast(n_id);
                longestPathInComponentRecursive(n_id, longestPath, currentPath, visited);
                currentPath.removeLast();
            }
        }
        visited.put(start, false);
    }

    public Collection<ID_type> longestSimplePath(Collection<ID_type> component) {
        ArrayList<ID_type> componentNodeIDs = new ArrayList<>(component);
        LinkedList<ID_type> currentPath = new LinkedList<>();
        LinkedList<ID_type> bestPath = new LinkedList<>();
        HashMap<ID_type, Boolean> visited = new HashMap<>();
        for(var node_id : componentNodeIDs){
            visited.put(node_id, false);
        }

        for(var node_id : componentNodeIDs) {
            currentPath.add(node_id);
            longestPathInComponentRecursive(node_id, bestPath, currentPath, visited);
            currentPath.clear();
        }

        return bestPath;
    }

    public boolean hasNode(ID_type id) {
        return nodes.containsKey(id);
    }

    public boolean hasEdge(ID_type id1, ID_type id2){
        return adjacencyList.get(id1).contains(id2);
    }
}

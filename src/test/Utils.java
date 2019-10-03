package test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {
    public static Position posWithLowerCost(LinkedList<Position> openList, Map<Position, Double> fScore) {
        Position lower = openList.get(0);
        for (Position node : openList) {
            if (!fScore.containsKey(node)) {
                fScore.put(node, Double.POSITIVE_INFINITY);
            }
            if (fScore.get(node) < fScore.get(lower)) {
                lower = node;
            }
        }
        return lower;
    }

    public static List<Position> getNeighbours(Position node) {
        List<Position> neighbours = new LinkedList<Position>();
        int newX = node.x;
        int newY = node.y - 1;
        if (Utils.isInBoundaries(newX, newY)) {
            neighbours.add(new Position(newX, newY));
        }
        newX = node.x;
        newY = node.y + 1;
        if (Utils.isInBoundaries(newX, newY)) {
            neighbours.add(new Position(newX, newY));
        }
        newX = node.x - 1;
        newY = node.y;
        if (Utils.isInBoundaries(newX, newY)) {
            neighbours.add(new Position(newX, newY));
        }
        newX = node.x + 1;
        newY = node.y;
        if (Utils.isInBoundaries(newX, newY)) {
            neighbours.add(new Position(newX, newY));
        }
        return neighbours;
    }

    public static boolean isInBoundaries(int x, int y) {
        return x >= 0 && y >= 0 && x < Main.sizeW && y < Main.sizeH;
    }

    public static LinkedList<Position> reconstructPath(Map<Position, Position> cameFrom, Position current) {
        LinkedList<Position> path = new LinkedList<Position>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        path = reverseLinkedList(path);
        path.removeFirst();
        return path;
    }

    public static LinkedList<Position> reverseLinkedList(LinkedList<Position> llist) {
        LinkedList<Position> revLinkedList = new LinkedList<Position>();
        for (int i = llist.size() - 1; i >= 0; i--) {
            revLinkedList.add(llist.get(i));
        }
        return revLinkedList;
    }

    public static double calculateDistance(Position pos1, Position pos2) {
        return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y);
    }
}

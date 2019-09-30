package test;

import jade.core.Agent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PuzzleAgent extends Agent {
    private Position actualPos;
    private Position goalPos;
    private Grid grid;
    public boolean goalReached;

    @Override
    protected void setup(){
        Object[] args = this.getArguments();
        if (args != null && args.length > 0) {
            this.actualPos = (Position) args[0];
            this.goalPos = (Position) args[1];
            this.grid = (Grid) args[2];
        } else {
            System.err.println("Impossible to create agent if grid is not set");
            this.doDelete();
        }
        System.out.println("Hi, I'm " + this.getLocalName() + ", " + this.actualPos + ", " + this.goalPos);
        this.grid.registerAgent(this);
        this.goalReached = false;
        this.live();
    }

    private void live() {
        while (!this.actualPos.equals(this.goalPos)) {
            //System.out.println("Actual pos : " + this.actualPos + ", goal pos : " + this.goalPos);
            this.goTo(this.goalPos);
        }
    }

    private void goTo(Position pos) {
        List<Position> path = null;
        while(path == null) {
            path = this.aStar(this.actualPos, pos);
        }
        for (Position node : path) {
            if (!this.grid.agentIn(node)) {
                this.actualPos.x = node.x;
                this.actualPos.y = node.y;
            } else {
                System.out.println("Agent in path, " + this.getLocalName());
                return;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Goal reached, " + this.getLocalName());
        this.goalReached = true;
    }

    @Override
    protected void takeDown() {
        System.out.println("Taking down " + this.getLocalName());
        super.takeDown();
    }

    private List<Position> aStar(Position start, Position goal) {
        LinkedList<Position> openList = new LinkedList<Position>();
        LinkedList<Position> closedList = new LinkedList<Position>();
        openList.add(start); // add starting node to open list

        Map<Position, Position> cameFrom = new HashMap<Position, Position>();

        Map<Position, Double> gScore = new HashMap<Position, Double>();
        gScore.put(start, 0.0);

        Map<Position, Double> fScore = new HashMap<Position, Double>();
        fScore.put(start, Utils.calculateDistance(start, goal));

        Position current;
        while (!openList.isEmpty()) {
            current = Utils.posWithLowerCost(openList, fScore); // get node with lowest fCosts from openList
            if (current.equals(goal)) {
                return Utils.reconstructPath(cameFrom, current);
            }

            openList.remove(current);
            closedList.add(current);
            for (Position neighbour : Utils.getNeighbours(current)) {
                if (closedList.contains(neighbour)) {
                    continue;
                }
                double tentativeGScore = gScore.get(current) + Utils.calculateDistance(current, neighbour);
                if (this.grid.agentIn(neighbour)) {
                    tentativeGScore = Double.POSITIVE_INFINITY;
                }
                if (!gScore.containsKey(neighbour)) {
                    gScore.put(neighbour, Double.POSITIVE_INFINITY);
                }
                if (tentativeGScore < gScore.get(neighbour)) {
                    cameFrom.put(neighbour, current);
                    gScore.put(neighbour, tentativeGScore);
                    fScore.put(neighbour, gScore.get(neighbour) + Utils.calculateDistance(neighbour, goal));
                    if (!openList.contains(neighbour)) {
                        openList.add(neighbour);
                    }
                }
            }
        }
        return null; // unreachable
    }

    public Position getActualPos() {
        return this.actualPos;
    }
}

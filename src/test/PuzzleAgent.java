package test;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PuzzleAgent extends Agent {
    private Position actualPos;
    private Position goalPos;
    private Grid grid;

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
        this.live();
    }

    private void live() {
        while (!this.grid.isPuzzleSolved()) {
            if (!this.actualPos.equals(this.goalPos)) {
                this.goTo(this.goalPos);
                this.moveIfRequested();
            } else {
                this.moveIfRequested();
            }
        }
        System.out.println("Finished");
        this.doDelete();
    }

    private void moveIfRequested() {
        boolean receiveMsg = true;
        while (receiveMsg) {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] infos = msg.getContent().split("=");
                System.out.println(this.getLocalName() + " requested to " + msg.getContent());
                if (infos[0].equals("gtfo_from")) {
                    if (this.actualPos.toString().equals(infos[1])) {
                        if (!this.randomMove()) {
                            this.requestToMoveAround();
                        }
                    } // Else : already moved from there
                } else {
                    System.err.println("Weird msg : " + msg.getContent());
                }
            } else {
                receiveMsg = false;
            }
        }
    }

    private void requestToMoveAround() {
        int newX = this.actualPos.x;
        int newY = this.actualPos.y - 1;
        Position pos = new Position(newX, newY);
        String agentName = this.grid.agentIn(pos);
        if (Utils.isInBoundaries(newX, newY) && agentName != "") {
            System.out.println(this.getLocalName() + " can't move");
            this.requestToMoveFrom(agentName, pos);
            return;
        }
        newX = this.actualPos.x;
        newY = this.actualPos.y + 1;
        pos = new Position(newX, newY);
        agentName = this.grid.agentIn(pos);
        if (Utils.isInBoundaries(newX, newY) && agentName != "") {
            System.out.println(this.getLocalName() + " can't move");
            this.requestToMoveFrom(agentName, pos);
            return;
        }
        newX = this.actualPos.x - 1;
        newY = this.actualPos.y;
        pos = new Position(newX, newY);
        agentName = this.grid.agentIn(pos);
        if (Utils.isInBoundaries(newX, newY) && agentName != "") {
            System.out.println(this.getLocalName() + " can't move");
            this.requestToMoveFrom(agentName, pos);
            return;
        }
        newX = this.actualPos.x + 1;
        newY = this.actualPos.y;
        pos = new Position(newX, newY);
        agentName = this.grid.agentIn(pos);
        if (Utils.isInBoundaries(newX, newY) && agentName != "") {
            System.out.println(this.getLocalName() + " can't move");
            this.requestToMoveFrom(agentName, pos);
            return;
        }
    }

    private boolean randomMove() {
        int newX = this.actualPos.x;
        int newY = this.actualPos.y - 1;
        if (Utils.isInBoundaries(newX, newY) && this.grid.agentIn(new Position(newX, newY)) == "") {
            this.actualPos.y--;
            System.out.println(this.getLocalName() + " moves to x:" + newX + ",y:" + newY);
            this.sleep(2);
            return true;
        }
        newX = this.actualPos.x;
        newY = this.actualPos.y + 1;
        if (Utils.isInBoundaries(newX, newY) && this.grid.agentIn(new Position(newX, newY)) == "") {
            this.actualPos.y++;
            System.out.println(this.getLocalName() + " moves to x:" + newX + ",y:" + newY);
            this.sleep(2);
            return true;
        }
        newX = this.actualPos.x - 1;
        newY = this.actualPos.y;
        if (Utils.isInBoundaries(newX, newY) && this.grid.agentIn(new Position(newX, newY)) == "") {
            this.actualPos.x--;
            System.out.println(this.getLocalName() + " moves to x:" + newX + ",y:" + newY);
            this.sleep(2);
            return true;
        }
        newX = this.actualPos.x + 1;
        newY = this.actualPos.y;
        if (Utils.isInBoundaries(newX, newY) && this.grid.agentIn(new Position(newX, newY)) == "") {
            this.actualPos.x++;
            System.out.println(this.getLocalName() + " moves to x:" + newX + ",y:" + newY);
            this.sleep(2);
            return true;
        }
        return false;
    }

    private void sleep(int multiplier) {
        try {
            TimeUnit.MILLISECONDS.sleep(Main.agentsSleepTime * multiplier);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void goTo(Position pos) {
        List<Position> path = null;
        while(path == null) {
            path = this.aStar(this.actualPos, pos);
        }
        for (Position node : path) {
            String agentInPath = this.grid.agentIn(node);
            if (agentInPath.equals("")) {
                this.actualPos.x = node.x;
                this.actualPos.y = node.y;
            } else {
                this.requestToMoveFrom(agentInPath, node);
                return;
            }
            this.sleep(1);
        }
    }

    private void requestToMoveFrom(String receiverName, Position pos) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID(receiverName,AID.ISLOCALNAME));
        msg.setLanguage("English");
        msg.setContent("gtfo_from=" + pos);
        System.out.println(this.getLocalName() + " requests " + receiverName + " to gtfo from " + pos);
        send(msg);
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
                if (this.grid.agentIn(neighbour) != "") {
                    tentativeGScore += 10;
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

    public Position getGoalPos() {
        return this.goalPos;
    }
}

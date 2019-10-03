package test;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PuzzleAgent extends Agent {
    private Position actualPos;
    private Position goalPos;
    private Grid grid;
    private ArrayList<Position> lastPositions = new ArrayList<>();

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
        this.sleep(2);
        while (!this.grid.isPuzzleSolved()) {
            if (!this.actualPos.equals(this.goalPos)) {
                this.goTo(this.goalPos);
            }
            this.moveIfRequested();
        }
        this.doDelete();
    }

    private void moveIfRequested() {
        boolean receiveMsg = true;
        while (receiveMsg) {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] infos = msg.getContent().split("=");
                if (infos[0].equals("gtfo_from")) {
                    if (this.actualPos.toString().equals(infos[1])) {
                        if (!this.randomMove()) {
                            this.requestNeighbourToMove();
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

    private void requestNeighbourToMove() {
        List<Position> poses = new ArrayList<>();
        poses.add(new Position(this.actualPos.x + 1, this.actualPos.y));
        poses.add(new Position(this.actualPos.x - 1, this.actualPos.y));
        poses.add(new Position(this.actualPos.x, this.actualPos.y + 1));
        poses.add(new Position(this.actualPos.x, this.actualPos.y - 1));
        Collections.shuffle(poses, new Random());
        for (Position pos : poses) {
            PuzzleAgent agent = this.grid.agentIn(pos);
            if (Utils.isInBoundaries(pos.x, pos.y) && agent != null) {
                this.requestToMoveFrom(agent.getLocalName(), pos);
                return;
            }
        }
    }

    private boolean randomMove() {
        List<Position> poses = new ArrayList<>();
        poses.add(new Position(this.actualPos.x + 1, this.actualPos.y));
        poses.add(new Position(this.actualPos.x - 1, this.actualPos.y));
        poses.add(new Position(this.actualPos.x, this.actualPos.y + 1));
        poses.add(new Position(this.actualPos.x, this.actualPos.y - 1));
        Collections.shuffle(poses, new Random());
        for (Position pos : poses) {
            PuzzleAgent agent = this.grid.agentIn(pos);
            if (Utils.isInBoundaries(pos.x, pos.y) && agent == null) {
                this.moveTo(pos);
                this.sleep(2);
                return true;
            }
        }
        return false;
    }

    private void moveTo(Position pos) {
        this.actualPos.x = pos.x;
        this.actualPos.y = pos.y;
        this.lastPositions.add(new Position(this.actualPos.x, this.actualPos.y));
        if (this.lastPositions.size() > Main.historySize) {
            this.lastPositions.remove(0);
        }
    }

    private void sleep(int multiplier) {
        try {
            TimeUnit.MILLISECONDS.sleep(Main.agentsSleepTime * multiplier);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void goTo(Position pos) {
        List<Position> path = this.aStar(this.actualPos, pos);
        for (Position node : path) {
            PuzzleAgent agent = this.grid.agentIn(node);
            if (agent == null) {
                this.moveTo(node);
            } else {
                this.requestToMoveFrom(agent.getLocalName(), node);
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
        send(msg);
    }

    @Override
    protected void takeDown() {
        //System.out.println("Taking down " + this.getLocalName());
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
                PuzzleAgent agent = this.grid.agentIn(neighbour);
                if (agent != null) {
                    tentativeGScore += Main.agentWeight;
                    if (agent.getActualPos().equals(agent.getGoalPos())) {
                        tentativeGScore += Main.reachedGoalWeight;
                    }
                }
                if (this.lastPositions.contains(current) && !current.equals(goal)) {
                    tentativeGScore += Collections.frequency(this.lastPositions, current) * 10;
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

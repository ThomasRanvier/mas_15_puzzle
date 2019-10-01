package test;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Grid {
    Runtime runtime;
    private final Lock lock = new ReentrantLock(true);
    private Set<PuzzleAgent> puzzleAgents = new HashSet<>();
    private boolean puzzleSolved;

    public Grid() {
        puzzleSolved = false;
        ContainerController containerController = this.initJade();
        this.initialiseMap(containerController);
    }

    private void initialiseMap(ContainerController containerController) {
        Random randomiser = new Random();
        Set<Position> initCells = new HashSet<>();
        Set<Position> goalCells = new HashSet<>();
        for (int x = 0; x < Main.size; x++) {
            for (int y = 0; y < Main.size; y++) {
                initCells.add(new Position(x, y));
                goalCells.add(new Position(x, y));
            }
        }
        for (int i = 1; i <= Main.agentsNumber; i++) {
            int size = initCells.size();
            Position initCell = null;
            Position goalCell = null;
            int item = randomiser.nextInt(size);
            int l = 0;
            for(Position cell : initCells) {
                if (l == item) {
                    initCell = cell;
                }
                l++;
            }
            item = randomiser.nextInt(size);
            l = 0;
            for(Position cell : goalCells) {
                if (l == item) {
                    goalCell = cell;
                }
                l++;
            }
            AgentController agentController;
            try {
                Object[] agentArgs = {initCell, goalCell, this};
                agentController = containerController.createNewAgent(Main.agentPrefix + i, PuzzleAgent.class.getName(), agentArgs);
                agentController.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
            initCells.remove(initCell);
            goalCells.remove(goalCell);
        }
    }

    public void registerAgent(PuzzleAgent puzzleAgent) {
        lock.lock();
        try {
            this.puzzleAgents.add(puzzleAgent);
        } catch (Exception e) {
            System.err.println("Several threads trying to access method registerAgent");
        } finally {
            lock.unlock();
        }
    }

    private ContainerController initJade() {
        this.runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "true");
        ContainerController containerController = runtime.createMainContainer(profile);
        return containerController;
    }

    public String agentIn(Position pos) {
        lock.lock();
        try {
            for (PuzzleAgent a : this.puzzleAgents) {
                if (a.getActualPos().x == pos.x && a.getActualPos().y == pos.y) {
                    return a.getLocalName();
                }
            }
            return "";
        } catch (Exception e) {
            System.err.println("Several threads trying to access method registerAgent");
        } finally {
            lock.unlock();
        }
        return "";
    }

    public boolean isPuzzleSolved() {
        lock.lock();
        try {
            if (this.puzzleSolved) {
                return true;
            }
            for (PuzzleAgent a : this.puzzleAgents) {
                if (!a.getActualPos().equals(a.getGoalPos())) {
                    return false;
                }
            }
            this.puzzleSolved = true;
            return true;
        } catch (Exception e) {
            System.err.println("Several threads trying to access method registerAgent");
        } finally {
            lock.unlock();
        }
        return this.puzzleSolved;
    }

    public HashSet<PuzzleAgent> getAgents() {
        lock.lock();
        try {
            return (HashSet<PuzzleAgent>) this.puzzleAgents;
        } catch (Exception e) {
            System.err.println("Several threads trying to access method registerAgent");
        } finally {
            lock.unlock();
        }
        return (HashSet<PuzzleAgent>) null;
    }
}

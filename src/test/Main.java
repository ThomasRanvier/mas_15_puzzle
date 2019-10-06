package test;

import java.util.concurrent.TimeUnit;

public class Main {
    public static final int sizeW = 5;
    public static final int sizeH = 5;
    public static final int renderRatio = 150;
    public static final int historySize = (sizeH * sizeW) / 4;
    public static final int agentWeight = 10;
    public static final int reachedGoalWeight = 0;//Set this to 0, it is less effective otherwise
    public static final int wantedAgentsNumber = 20;
    public static final int agentsNumber = Math.max(1, Math.min(sizeW * sizeH - 1, wantedAgentsNumber));
    public static final int minAgentsSleepTime = 15;//Do not decrease value, needed to let time to switch thread
    // after a move, also to let the time to every agent to be created before checking if the puzzle is solved
    public static final int wantedAgentsSleepTime = 0;
    public static final int agentsSleepTime = Math.max(wantedAgentsSleepTime, minAgentsSleepTime);
    public static final String agentPrefix = "agent_";
    public static void main(String[] args) {
        Grid grid = new Grid();
        Renderer renderer = new Renderer(grid);
        renderer.start();
    }
}

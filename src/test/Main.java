package test;

public class Main {
    public static final int size = 5;
    public static final int renderRatio = 100;
    public static final int agentsNumber = 20;
    public static final String agentPrefix = "agent_";
    public static void main(String[] args) {
        Grid grid = new Grid();
        Renderer renderer = new Renderer(grid);
        renderer.start();
    }
}

package test;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class Renderer extends Thread {
    private Grid grid;
    private HashSet<PuzzleAgent> agents;

    public Renderer(Grid grid) {
        this.grid = grid;
        this.agents = this.grid.getAgents();
    }

    public void run() {
        JFrame frame = new JFrame("Visualisation");

        frame.setLayout(new GridLayout(Main.sizeH, Main.sizeW));
        frame.setVisible(true);
        JLabel[][] labels = new JLabel[Main.sizeW][Main.sizeH];
        for (int y = 0; y < Main.sizeH; y++) {
            for (int x = 0; x < Main.sizeW; x ++) {
                labels[x][y] = new JLabel();
                labels[x][y].setOpaque(true);
                labels[x][y].setMinimumSize(new Dimension(Main.renderRatio, Main.renderRatio));
                labels[x][y].setPreferredSize(new Dimension(Main.renderRatio, Main.renderRatio));
                labels[x][y].setMaximumSize(new Dimension(Main.renderRatio, Main.renderRatio));
                labels[x][y].setText("x:" + x + ",y:" + y);
                frame.add(labels[x][y]);
            }
        }
        while (true) {
            for (int x = 0; x < Main.sizeW; x ++) {
                for (int y = 0; y < Main.sizeH; y++) {
                    labels[x][y].setText("");
                    labels[x][y].setBackground(Color.white);
                }
            }
            for (PuzzleAgent agent : this.agents) {
                Position actualPos = agent.getActualPos();
                labels[actualPos.x][actualPos.y].setText(agent.getLocalName());
                labels[actualPos.x][actualPos.y].setBackground(agent.getActualPos().equals(agent.getGoalPos()) ? Color.green : Color.red);
            }
            frame.repaint();
            frame.pack();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

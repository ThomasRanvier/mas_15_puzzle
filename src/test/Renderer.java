package test;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
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

        frame.setLayout(new GridLayout(Main.size, Main.size));
        frame.setVisible(true);
        JLabel[][] labels = new JLabel[Main.size][Main.size];
        for (int x = 0; x < Main.size; x ++) {
            for (int y = 0; y < Main.size; y++) {
                labels[x][y] = new JLabel();
                labels[x][y].setOpaque(true);
                labels[x][y].setMinimumSize(new Dimension(Main.renderRatio, Main.renderRatio));
                labels[x][y].setPreferredSize(new Dimension(Main.renderRatio, Main.renderRatio));
                labels[x][y].setMaximumSize(new Dimension(Main.renderRatio, Main.renderRatio));
                frame.add(labels[x][y]);
            }
        }
        while (true) {
            for (int x = 0; x < Main.size; x ++) {
                for (int y = 0; y < Main.size; y++) {
                    labels[y][x].setText("");
                    labels[y][x].setBackground(Color.white);
                }
            }
            for (PuzzleAgent agent : this.agents) {
                Position actualPos = agent.getActualPos();
                labels[actualPos.y][actualPos.x].setText(agent.getLocalName());
                labels[actualPos.y][actualPos.x].setBackground(agent.goalReached ? Color.green : Color.red);
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

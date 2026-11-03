import javax.swing.*;
import java.awt.*;

public class Visualizer extends JFrame {

    final int MAX_X = (int) getToolkit().getScreenSize().getWidth();
    final int MAX_Y = (int) getToolkit().getScreenSize().getHeight();

    private GraphicsPanel panel;
    private String twoDstring;
    private int rows, cols;

    Visualizer(String twoDstring) {
        setMapData(twoDstring);

        this.panel = new GraphicsPanel();
        this.panel.setBackground(Color.LIGHT_GRAY);
        this.getContentPane().add(BorderLayout.CENTER, panel);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(MAX_X / 4, MAX_Y / 2);
        this.setLocationRelativeTo(null);

        this.setVisible(true);
    }

    private void setMapData(String map) {
        this.twoDstring = map;
        String[] lines = map == null ? new String[0] : map.split("\n");
        this.rows = lines.length;
        this.cols = 0;
        for (String line : lines) {
            this.cols = Math.max(this.cols, line.length());
        }
    }

    private class GraphicsPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (twoDstring == null || twoDstring.isEmpty()
                    || rows == 0 || cols == 0) {
                return;
            }

            String[] lines = twoDstring.split("\n");

            // Calculate a dynamic grid size that fits the entire map.
            double cellWidth = (double) getWidth() / cols;
            double cellHeight = (double) getHeight() / rows;

            // Keep cells square and fit the entire map inside the window.
            double gridSize = Math.min(cellWidth, cellHeight);
            if (gridSize <= 0) return;

            // Calculate the map dimensions.
            double mapWidth = cols * gridSize;
            double mapHeight = rows * gridSize;

            // Center the map inside the panel.
            double startX = (getWidth() - mapWidth) / 2.0;
            double startY = (getHeight() - mapHeight) / 2.0;

            Graphics2D g2 = (Graphics2D) g.create();


                // Draw neighbourhood colours first.
                for (int row = 0; row < rows; row++) {
                    String line = lines[row];

                    for (int col = 0; col < line.length(); col++) {

                        char ch = line.charAt(col);

                        if (ch == Const.INFECTED) {
                            g2.setColor(Const.INFECTED_CLR);

                        } else if (ch == Const.RESISTANT) {
                            g2.setColor(Const.RESISTANT_CLR);

                        } else if (ch == Const.VACCINATED) {
                            g2.setColor(Const.VACCINATED_CLR);

                        } else if (ch == Const.SUSCEPTIBLE) {
                            g2.setColor(Const.SUSCEPTIBLE_CLR);

                        } else {
                            g2.setColor(Const.EMPTY_CLR);
                        }

                        // Calculate cell boundaries independently.
                        int x1 = (int) Math.round(startX + col * gridSize);
                        int y1 = (int) Math.round(startY + row * gridSize);
                        int x2 = (int) Math.round(startX + (col + 1) * gridSize);
                        int y2 = (int) Math.round(startY + (row + 1) * gridSize);

                        // Fill exactly within the calculated cell boundaries.
                        g2.fillRect(x1, y1, x2 - x1, y2 - y1);
                    }
                }

                // Keep grid lines around the map instead of extending across empty margins.
                g2.setColor(Color.BLACK);

                for (double x = startX; x <= startX + mapWidth; x += gridSize) {
                    int px = (int) Math.round(x);
                    g2.drawLine(px, (int) Math.round(startY), px,
                            (int) Math.round(startY + mapHeight));
                }

                for (double y = startY; y <= startY + mapHeight; y += gridSize) {
                    int py = (int) Math.round(y);
                    g2.drawLine((int) Math.round(startX), py,
                            (int) Math.round(startX + mapWidth), py);
                }
                g2.dispose();
        }
    }
}

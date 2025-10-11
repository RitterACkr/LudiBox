package ludibox.game.fourinarow;

import ludibox.core.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class FourInARowPanel extends GamePanel {

    // -- Constants --
    private static final int COLS = 7;
    private static final int ROWS = 6;
    private static final int MARGIN = 10;
    private static final int CELL_SIZE = 70;
    private static final int ARROW_HEIGHT = 40;

    // -- State --
    // 0: empty, 1: red, 2: yellow
    private final int[][] board = new int[ROWS][COLS];

    // -- UI --
    private JPanel boardPanel;

    private Color PIECE_RED = new Color(220, 20, 60);
    private Color PIECE_YELLOW = new Color(255, 215, 0);

    public FourInARowPanel(ludibox.core.MainWindow m) {
        super(m);
        this.setLayout(new BorderLayout());
        this.setBackground(Color.LIGHT_GRAY);



        // テスト
        board[5][3] = 1;
        board[4][3] = 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boardWidth = COLS * CELL_SIZE;
        int boardHeight = ROWS * CELL_SIZE;

        // centered
        int x0 = (getWidth() - boardWidth) / 2;
        int y0 = getHeight() - boardHeight - 10;

        // pieces
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int piece = board[r][c];
                if (piece == 0) continue;

                int x = x0 + c * CELL_SIZE + MARGIN;
                int y = y0 + r * CELL_SIZE + MARGIN;
                int d = CELL_SIZE - MARGIN * 2;

                Color base = (piece == 1) ? PIECE_RED : PIECE_YELLOW;
                Color highlight = base.brighter();
                Color shadow = base.darker();

                GradientPaint gp = new GradientPaint(
                        x, y, highlight, x, y + d, shadow
                );
                g2d.setPaint(gp);
                g2d.fillOval(x, y, d, d);
            }
        }

        // board
        RoundRectangle2D boardRect = new RoundRectangle2D.Float(x0, y0, boardWidth, boardHeight, 20, 20);
        Area boardArea = new Area(boardRect);

        // hole
        int diameter = CELL_SIZE - MARGIN * 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int cx = x0 + c * CELL_SIZE + MARGIN;
                int cy = y0 + r * CELL_SIZE + MARGIN;
                boardArea.subtract(new Area(new Ellipse2D.Float(cx, cy, diameter, diameter)));
            }
        }

        // fill
        g2d.setColor(new Color(60, 60, 60));
        g2d.fill(boardArea);

        g2d.dispose();
    }
}

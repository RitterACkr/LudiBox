package ludibox.game.snakegame;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.math.Vec2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Timer;

public class SnakeGamePanel extends GamePanel {
    // データ
    private SnakeBoard board;
    private final Deque<Vec2> snake = new ArrayDeque<>();
    private Vec2 food;          // Food座標
    private int dx = 1, dy = 0; // 進行方向

    // Timer
    private Timer timer;

    public SnakeGamePanel(MainWindow m) {
        super(m);

        init();
    }

    /* 初期化処理 */
    private void init() {
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.GREEN.darker());
        this.setFocusable(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = .2; gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(Box.createHorizontalStrut(40), gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        board = new SnakeBoard();
        this.add(board, gbc);

        gbc.gridx = 2; gbc.weightx = 1.; gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(Box.createHorizontalStrut(100), gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private class SnakeBoard extends JPanel {
        // サイズ
        private static final int TILE_SIZE = 24;
        private static final int ROWS = 20;
        private static final int COLS = 20;

        // 内部情報
        private Vec2 food;
        private Deque<Vec2> snake;

        public SnakeBoard() {
            this.setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
            this.setBackground(Color.GREEN.brighter());
        }

        public void setFood(Vec2 food) { this.food = food; }
        public void setSnake(Deque<Vec2> snake) { this.snake = snake; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Grid
            g.setColor(Color.DARK_GRAY);
            for (int i = 1; i < COLS; i++)
                g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, ROWS * TILE_SIZE);
            for (int i = 1; i < ROWS; i++)
                g.drawLine(0, i * TILE_SIZE, COLS * TILE_SIZE, i * TILE_SIZE);

            // food
            if (food != null) {
                g.setColor(Color.RED);
                g.fillRect((int) (food.x * TILE_SIZE), (int) (food.y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
            }

            // snake
            if (snake != null) {
                g.setColor(Color.BLUE.darker());
                for (Vec2 v : snake) {
                    g.fillRect((int) (v.x * TILE_SIZE), (int) (v.y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }
}

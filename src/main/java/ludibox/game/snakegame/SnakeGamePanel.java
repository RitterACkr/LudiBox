package ludibox.game.snakegame;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.math.Vec2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayDeque;
import java.util.Deque;

public class SnakeGamePanel extends GamePanel implements ActionListener, KeyListener {
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

        start();
    }

    /* 初期化処理 */
    private void init() {
        this.setLayout(new GridBagLayout());
        this.setBackground(Color.GREEN.darker());
        this.setFocusable(true);
        this.addKeyListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = .2; gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(Box.createHorizontalStrut(40), gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        board = new SnakeBoard();
        this.add(board, gbc);

        gbc.gridx = 2; gbc.weightx = 1.; gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(Box.createHorizontalStrut(100), gbc);

        // snake
        snake.clear();
        snake.add(new Vec2(5, 10));
        snake.add(new Vec2(4, 10));
        snake.add(new Vec2(3, 10));

        board.setSnake(snake);
    }

    private void start() {
        timer = new Timer(300, this);
        timer.start();
        requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert snake.peekFirst() != null;
        Vec2 head = new Vec2(snake.peekFirst());

        head.translate(dx, dy);

        // 衝突判定
        if (board.checkWallCollision(head) || snake.contains(head)) {
            System.out.println("GAME OVER");
            return;
        }

        // 先頭の更新
        snake.addFirst(head);

        // Foodとの判定
        if (head.equals(food)) {
            System.out.println("FOOD処理");
        } else {
            // 最後尾の削除
            snake.removeLast();
        }

        board.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                if (dy == 0) { dx = 0; dy = -1; }
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                if (dy == 0) { dx = 0; dy = 1; }
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                if (dx == 0) { dx = -1; dy = 0; }
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                if (dx == 0) { dx = 1; dy = 0; }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    private class SnakeBoard extends JPanel {
        // サイズ
        private static final int TILE_SIZE = 24;
        public static final int ROWS = 20;
        public static final int COLS = 20;

        // 内部情報
        private Vec2 food;
        private Deque<Vec2> snake;

        public SnakeBoard() {
            this.setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
            this.setBackground(Color.GREEN.brighter());
        }

        public void setFood(Vec2 food) { this.food = food; }
        public void setSnake(Deque<Vec2> snake) { this.snake = snake; }

        /* 壁との衝突判定 */
        private boolean checkWallCollision(Vec2 pos) {
            return pos.x < 0. || pos.x >= COLS || pos.y < 0. || pos.y >= ROWS;
        }

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

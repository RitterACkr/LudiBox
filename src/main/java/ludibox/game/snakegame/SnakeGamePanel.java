package ludibox.game.snakegame;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.math.Vec2;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;
import ludibox.util.ImageLoader;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

public class SnakeGamePanel extends GamePanel implements KeyListener {
    // データ
    private SnakeBoard board;
    private final Deque<Vec2> snake = new ArrayDeque<>();
    private int dx = 1, dy = 0;     // 現在の進行方向
    private int ndx = 1, ndy = 0;   // 次の進行方向

    // Timer
    private Timer timer;
    private final int STEP_INTERVAL = 160; // 現在のステップ時間
    private boolean isStarted = false;

    // animation
    private Timer animationTimer;
    private float animeProgress = 1f;
    private final int frameCount = 12;   // アニメーション分割数
    private int currentFrame = 0;
    List<Vec2> prevSnake;

    // UI
    private InfoPanel infoPanel;


    public SnakeGamePanel(MainWindow m) {
        super(m);

        init();
    }

    /* 初期化処理 */
    private void init() {
        isStarted = false;
        animeProgress = 1f; currentFrame = 0;
        dx = 1; dy = 0; ndx = 1; ndy = 0;

        this.removeAll();
        this.revalidate();

        this.setLayout(new GridBagLayout());
        this.setBackground(Color.LIGHT_GRAY.darker());
        this.setFocusable(true);
        this.addKeyListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = .2; gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(Box.createHorizontalStrut(10), gbc);

        gbc.gridx = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        board = new SnakeBoard();
        this.add(board, gbc);

        gbc.gridx = 2; gbc.weightx = 1.; gbc.fill = GridBagConstraints.HORIZONTAL;
        infoPanel = new InfoPanel();
        this.add(infoPanel, gbc);

        // snake
        snake.clear();
        snake.add(new Vec2(5, 10));
        snake.add(new Vec2(4, 10));
        snake.add(new Vec2(3, 10));

        board.setSnake(snake);

        board.generateFood();

        board.setAnimation(new ArrayList<>(snake), new ArrayList<>(snake), animeProgress);
        board.repaint();
    }

    private void start() {
        timer = new Timer(STEP_INTERVAL, e -> {
            dx = ndx; dy = ndy;

            assert snake.peekFirst() != null;
            Vec2 head = new Vec2(snake.peekFirst());
            prevSnake = new ArrayList<>(snake);

            head.translate(dx, dy);

            // 衝突判定
            if (board.checkWallCollision(head) || snake.contains(head)) {
                finish();
                return;
            }

            // 先頭の更新
            snake.addFirst(head);

            // Foodとの判定
            if (board.checkFoodCollision(head)) {
                board.eatenFood++;
                board.generateFood();
                infoPanel.updateCount(board.getEatenFood());
            } else {
                // 最後尾の削除
                snake.removeLast();
            }

            // アニメーションの開始
            animeProgress = 0f;
            currentFrame = 0;
            startAnimation();
        });
        timer.start();
        requestFocusInWindow();
    }

    /* アニメーションの開始 */
    private void startAnimation() {
        int frameInterval = STEP_INTERVAL / frameCount;

        if (animationTimer != null) animationTimer.stop();

        animationTimer = new Timer(frameInterval, e -> {
            currentFrame++;
            animeProgress = (float) currentFrame / frameCount;

            if (animeProgress >= 1f) {
                animeProgress = 1f;
                animationTimer.stop();
            }

            board.setSnake(snake);
            board.setFood(board.food);
            board.setAnimation(new ArrayList<>(prevSnake), new ArrayList<>(snake), animeProgress);
            board.repaint();
        });

        animationTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isStarted) {
            isStarted = true;
            start();
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                if (dy == 0) { ndx = 0; ndy = -1; }
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                if (dy == 0) { ndx = 0; ndy = 1; }
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                if (dx == 0) { ndx = -1; ndy = 0; }
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                if (dx == 0) { ndx = 1; ndy = 0; }
            }
        }
    }

    /* 1ゲームの終了 */
    private void finish() {
        infoPanel.setGameOver();
        timer.stop();
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    protected void quit() {
        super.quit();
    }

    private class SnakeBoard extends JPanel {
        // サイズ
        private static final int TILE_SIZE = 28;
        public static final int ROWS = 16;
        public static final int COLS = 20;

        // 内部情報
        private final Image foodImg;
        private Vec2 food;
        private int eatenFood = 0;
        private Deque<Vec2> snake;

        // アニメーション情報
        private List<Vec2> prevSnake = new ArrayList<>();
        private List<Vec2> currentSnake = new ArrayList<>();
        private float animeProgress = 1f;

        public SnakeBoard() {
            this.setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
            this.setBackground(Color.LIGHT_GRAY.brighter());
            foodImg = ImageLoader.resizeByWidth(
                    ImageLoader.loadImage("snakegame/apple.png"),
                    TILE_SIZE
            );
        }

        public void setFood(Vec2 food) { this.food = food; }
        public void setSnake(Deque<Vec2> snake) { this.snake = snake; }
        public void setAnimation(List<Vec2> from, List<Vec2> to, float progress) {
            this.prevSnake = from; this.currentSnake = to; this.animeProgress = progress;
        }

        /* 壁との衝突判定 */
        private boolean checkWallCollision(Vec2 pos) {
            return pos.x < 0. || pos.x >= COLS || pos.y < 0. || pos.y >= ROWS;
        }

        /* foodとの衝突判定 */
        private boolean checkFoodCollision(Vec2 pos) {
            return food.equals(pos);
        }

        /* foodのランダム生成 */
        private void generateFood() {
            Random rand = new Random();
            while (true) {
                int x = rand.nextInt(COLS);
                int y = rand.nextInt(ROWS);
                Vec2 pos = new Vec2(x, y);

                if (!snake.contains(pos)) {
                    food = pos;
                    break;
                }
            }
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // background
            Color colorA = new Color(250, 248, 240);
            Color colorB = new Color(240, 235, 225);

            for (int y = 0; y < ROWS; y++) {
                for (int x = 0; x < COLS; x++) {
                    if ((x + y) % 2 == 0) g.setColor(colorA);
                    else g.setColor(colorB);
                    g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }

            // Grid
            g.setColor(Color.LIGHT_GRAY);
            for (int i = 1; i < COLS; i++)
                g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, ROWS * TILE_SIZE);
            for (int i = 1; i < ROWS; i++)
                g.drawLine(0, i * TILE_SIZE, COLS * TILE_SIZE, i * TILE_SIZE);

            // food
            if (food != null) {
                int x = (int) (food.x * TILE_SIZE);
                int y = (int) (food.y * TILE_SIZE);
                g.drawImage(foodImg, x, y, TILE_SIZE, TILE_SIZE, this);
            }

            // snake
            if (snake != null) {
                for (int i = 0; i < prevSnake.size(); i++) {
                    Vec2 from = prevSnake.get(i);
                    Vec2 to = currentSnake.get(i);

                    float x = (float) (from.x + (to.x - from.x) * animeProgress);
                    float y = (float) (from.y + (to.y - from.y) * animeProgress);

                    if (i == 0) g.setColor(new Color(60, 180, 60));
                    else g.setColor(new Color(40, 140, 40));

                    g.fillRect((int) (x * TILE_SIZE), (int) (y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
                }
            }
        }

        private int getEatenFood() { return eatenFood; }
    }

    private class InfoPanel extends JPanel {
        private final JLabel logoLabel;
        private final JLabel gameOverLabel;
        private final JLabel foodCountLabel;
        private final CustomButton restartButton;
        private final CustomButton exitButton;

        public InfoPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setPreferredSize(new Dimension(120, 500));
            this.setOpaque(false);

            // ロゴ表示
            Image image = ImageLoader.resizeByWidth(
                    ImageLoader.loadImage("snakegame/snake_logo.png"),
                    getPreferredSize().width
            );
            logoLabel = new JLabel(new ImageIcon(image));
            logoLabel.setAlignmentX(CENTER_ALIGNMENT);

            // game-over
            gameOverLabel = new JLabel("<html><br></html>", SwingConstants.CENTER);
            gameOverLabel.setFont(new Font("Mono", Font.BOLD, 28));
            gameOverLabel.setForeground(Color.RED.darker());
            gameOverLabel.setAlignmentX(CENTER_ALIGNMENT);

            // カウント
            Image foodImg = ImageLoader.resizeByWidth(
                    ImageLoader.loadImage("snakegame/apple.png"),
                    getPreferredSize().width / 2
            );
            foodCountLabel = new JLabel(new ImageIcon(foodImg));
            foodCountLabel.setText("x 0");
            foodCountLabel.setFont(new Font("Arial", Font.BOLD, 36));
            foodCountLabel.setAlignmentX(CENTER_ALIGNMENT);

            // 操作方法
            JLabel upLabel = createLabel("UP: ↑, W");
            JLabel downLabel = createLabel("DOWN: ↓, S");
            JLabel leftLabel = createLabel("LEFT: ←, A");
            JLabel rightLabel = createLabel("RIGHT: →, D");

            // ボタン
            restartButton = new CustomButton("RESTART", CustomButtonStyle.SIMPLE);
            restartButton.addActionListener(e -> init());
            restartButton.setAlignmentX(CENTER_ALIGNMENT);
            restartButton.setVisible(false);
            exitButton = new CustomButton("EXIT", CustomButtonStyle.SIMPLE);
            exitButton.addActionListener(e -> quit());
            exitButton.setAlignmentX(CENTER_ALIGNMENT);
            exitButton.setVisible(false);


            this.add(logoLabel);
            this.add(Box.createVerticalStrut(10));
            this.add(gameOverLabel);
            this.add(Box.createVerticalStrut(10));
            this.add(foodCountLabel);
            this.add(Box.createVerticalStrut(30));
            this.add(upLabel);
            this.add(downLabel);
            this.add(leftLabel);
            this.add(rightLabel);
            this.add(Box.createVerticalStrut(30));
            this.add(restartButton);
            this.add(Box.createVerticalStrut(10));
            this.add(exitButton);
        }

        /* カウント数の更新 */
        public void updateCount(int count) {
            foodCountLabel.setText("x " + count);
        }

        /* ゲームオーバー時の処理 */
        public void setGameOver() {
            gameOverLabel.setText("<html>GAME<br>OVER</html>");
            restartButton.setVisible(true);
            exitButton.setVisible(true);
        }

        /* ラベル設定の使いまわし */
        private JLabel createLabel(String text) {
            JLabel label = new JLabel(text, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 20));
            label.setAlignmentX(CENTER_ALIGNMENT);
            return label;
        }
    }
}

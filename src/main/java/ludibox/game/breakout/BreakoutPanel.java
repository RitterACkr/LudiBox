package ludibox.game.breakout;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BreakoutPanel extends GamePanel implements MouseMotionListener {

    // UI関連
    private int width, height;

    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel levelLabel;
    private CustomButton menuButton;

    // マップ
    private int[][] currentMap;

    // パドル
    private int paddleWidth = 100, paddleHeight = 10;
    private int paddleX, paddleY;

    // ボール
    private int ballX, ballY;
    private int ballSize = 12;
    private int ballDx = 4, ballDy = -8;


    // ステータス
    private boolean isRunning = false;
    private Timer timer;

    public BreakoutPanel(MainWindow m) {
        super(m);

        currentMap = loadLevelFromFile("breakout/levels.csv", 1);

        init();

        this.addMouseMotionListener(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isRunning) isRunning = true;
            }
        });
    }

    /* 初期化処理 */
    private void init() {
        width = window.getWidth();
        height = window.getHeight();

        this.setPreferredSize(new Dimension(width, height));
        this.setLayout(new BorderLayout());

        // 上部UI Panel
        JPanel topPanel = new JPanel(new GridLayout(1, 4));
        // Labels
        scoreLabel = createLabel("SCORE: 0");
        livesLabel = createLabel("LIVES: 3");
        levelLabel = createLabel("LEVEL: 1");

        // Menu Button
        menuButton = new CustomButton("Menu", CustomButtonStyle.SIMPLE);
        menuButton.setBounds(width - 120, 10, 80, 30);
        menuButton.addActionListener(e -> System.out.println("Menu button clicked"));
        this.add(menuButton);

        topPanel.add(scoreLabel);
        topPanel.add(livesLabel);
        topPanel.add(levelLabel);
        topPanel.add(menuButton);

        this.add(topPanel, BorderLayout.NORTH);

        // パドル初期位置
        paddleX = width / 2 - paddleWidth / 2;
        paddleY = height - 60;

        // ボール初期位置
        ballX = paddleX + paddleWidth / 2 - ballSize / 2;
        ballY = paddleY - ballSize;

        // タイマー初期化
        timer = new Timer(16, e -> {
            if (isRunning) {
                update();
                repaint();
            }
        });
        timer.start();
    }

    // ラベルの作成
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        return label;
    }

    private void update() {
        // ボール移動
        ballX += ballDx; ballY += ballDy;

        // 壁との反射
        if (ballX < 0 || ballX > width - ballSize * 2) ballDx = -ballDx;
        if (ballY < 40) ballDy = -ballDy;

        // 画面下で停止
        if (ballY > height - ballSize) {
            isRunning = false;
            ballX = paddleX + paddleWidth / 2 - ballSize / 2;
            ballY = paddleY - ballSize;
        }

        // パドルとの衝突
        Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
        Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

        if (ballRect.intersects(paddleRect)) {
            ballY = paddleY - ballSize; // パドル上に戻す

            // パドル中心とボール中心の差から反射角度の調整
            int paddleCenter = paddleX + paddleWidth / 2;
            int ballCenter = ballX + ballSize / 2;
            int diff = ballCenter - paddleCenter;

            // 最大反射速度
            int maxDx = 12;
            ballDx = (int) ((double) diff / (paddleWidth / 2) * maxDx);

            // 上方向に返す
            ballDy = -ballDy;
        }
    }

    // 指定されたレベルのブロック情報を読み込む
    private int[][] loadLevelFromFile(String fileName, int targetLevel) {
        List<int[]> currentLevel = new ArrayList<>();
        boolean inTargetLevel = false;  // レベル読み込み用のフラグ
        int currentLevelNum = 0;       // 現在参照中のレベル

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(fileName))
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // 空行はスキップ
                if (line.isEmpty()) continue;

                // #LEVEL行の処理
                if (line.startsWith("#LEVEL")) {
                    currentLevelNum++;
                    inTargetLevel = (currentLevelNum == targetLevel);
                    continue;
                }

                // targetLevelと一致するなら内容を保存
                if (inTargetLevel) {
                    String[] tokens = line.split(",");
                    int[] row = new int[tokens.length];
                    for (int i = 0; i < tokens.length; i++)
                        row[i] = Integer.parseInt(tokens[i].trim());
                    currentLevel.add(row);
                } else if (currentLevelNum > targetLevel) {
                    // 既に読み込んだレベルより後のレベルは無視
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading level file: " + fileName);
            e.printStackTrace();
        }

        if (currentLevel.isEmpty()) {
            System.err.println("No data found for level: LEVEL " + targetLevel);
            return new int[0][0];
        }

        return currentLevel.toArray(new int[0][]);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 背景
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, window.getWidth(), window.getHeight());

        // UIテキスト
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("SCORE: 0, LIVES: 3, LEVEL: 1", 10, 25);

        // パドル描画
        g.setColor(Color.GREEN);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);

        // ボール描画
        g.setColor(Color.WHITE);
        g.fillOval(ballX, ballY, ballSize, ballSize);

        // ブロック描画
        if (currentMap != null) {
            int blockRows = currentMap.length;
            int blockCols = currentMap[0].length;
            int blockWidth = width / blockCols;
            int blockHeight = 20;

            g.setColor(Color.CYAN);
            for (int row = 0; row < blockRows; row++) {
                for (int col = 0; col < blockCols; col++) {
                    if (currentMap[row][col] != 0) {
                        int x = col * blockWidth;
                        int y = 60 + row * blockHeight;
                        g.fillRect(x + 1, y + 1, blockWidth - 2, blockHeight - 2);
                    }
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) {
        // パドルの中央をマウスに合わせる
        paddleX = e.getX() - paddleWidth / 2;

        if (paddleX < 0) paddleX = 0;
        if (paddleX > width - paddleWidth) paddleX = width - paddleWidth;

        // ゲームスタート前はボールをパドル上部に固定
        if (!isRunning) ballX = paddleX + paddleWidth / 2 - ballSize / 2;

        repaint();
    }
}

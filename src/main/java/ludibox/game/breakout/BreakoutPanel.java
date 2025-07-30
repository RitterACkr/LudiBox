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

public class BreakoutPanel extends GamePanel implements MouseMotionListener {

    // UI関連
    private int width, height;

    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel levelLabel;
    private CustomButton menuButton;

    // パドル
    private int paddleWidth = 100, paddleHeight = 10;
    private int paddleX, paddleY;

    // ボール
    private int ballX, ballY;
    private int ballSize = 10;
    private int ballDx = 4, ballDy = -4;


    // ステータス
    private boolean isRunning = false;
    private Timer timer;

    public BreakoutPanel(MainWindow m) {
        super(m);

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
    }

    @Override
    public void mouseDragged(MouseEvent e) { }

    @Override
    public void mouseMoved(MouseEvent e) {
        // パドルの中央をマウスに合わせる
        paddleX = e.getX() - paddleWidth / 2;

        if (paddleX < 0) paddleX = 0;
        if (paddleX > width - paddleWidth) paddleX = width - paddleWidth;

        repaint();
    }
}

package ludibox.game.breakout;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;

import javax.swing.*;
import java.awt.*;

public class BreakoutPanel extends GamePanel {

    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel levelLabel;
    private CustomButton menuButton;

    // パドル
    private int paddleWidth = 100;
    private int paddleHeight = 10;

    private int paddleX;
    private int paddleY;

    public BreakoutPanel(MainWindow m) {
        super(m);

        init();
    }

    /* 初期化処理 */
    private void init() {
        this.setPreferredSize(new Dimension(window.getWidth(), window.getHeight()));
        this.setLayout(new BorderLayout());

        // 上部UI Panel
        JPanel topPanel = new JPanel(new GridLayout(1, 4));
        // Labels
        scoreLabel = createLabel("SCORE: 0");
        livesLabel = createLabel("LIVES: 3");
        levelLabel = createLabel("LEVEL: 1");

        // Menu Button
        menuButton = new CustomButton("Menu", CustomButtonStyle.SIMPLE);
        menuButton.setBounds(window.getWidth() - 120, 10, 80, 30);
        menuButton.addActionListener(e -> System.out.println("Menu button clicked"));
        this.add(menuButton);

        topPanel.add(scoreLabel);
        topPanel.add(livesLabel);
        topPanel.add(levelLabel);
        topPanel.add(menuButton);

        this.add(topPanel, BorderLayout.NORTH);

        // パドル初期位置
        paddleX = window.getWidth() / 2 - paddleWidth / 2;
        paddleY = window.getHeight() - 60;
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
}

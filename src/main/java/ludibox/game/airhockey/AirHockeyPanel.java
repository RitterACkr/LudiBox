package ludibox.game.airhockey;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class AirHockeyPanel extends GamePanel implements MouseMotionListener {
    // タイマー
    private Field field;  // フィールド
    private Timer timer;

    public AirHockeyPanel(MainWindow m) {
        super(m);

        init();
    }

    // 初期化処理
    void init() {
        Dimension windowSize = window.getContentPane().getSize();
        int width = windowSize.width;
        int height = windowSize.height;
        this.setPreferredSize(new Dimension(width, height));

        int fieldWidth = width / 2;
        int fieldHeight = height;

        // フィールドの作成
        field = new Field(width, height, fieldWidth, fieldHeight);

        this.addMouseMotionListener(this);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // タイマーの初期化
        // 60FPS
        timer = new Timer(8, e -> {
            update();
            repaint();
        });

        timer.start();
    }

    // update
    void update() {
        field.update();
    }

    // draw
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        field.draw(g2d);
    }

    // Mouse
    @Override
    public void mouseMoved(MouseEvent e) {
        field.movePlayer(e.getX(), e.getY());
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}
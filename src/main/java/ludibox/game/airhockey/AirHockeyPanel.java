package ludibox.game.airhockey;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class AirHockeyPanel extends GamePanel implements MouseMotionListener {

    // ウィンドウサイズ
    int width, height;
    // フィールドのサイズ
    int fieldWidth, fieldHeight;

    // タイマー
    private Timer timer;

    private Field field;  // フィールド
    private Puck puck;  // パック
    private Mallet mallet; // マレット

    public AirHockeyPanel(MainWindow m) {
        super(m);

        init();
    }

    // 初期化処理
    void init() {
        width = window.getWidth();
        height = window.getHeight();

        fieldWidth = width / 2;
        fieldHeight = height;

        // フィールドの作成
        field = new Field(width, height, fieldWidth, fieldHeight);

        // パックの初期配置
        puck = new Puck((double) width / 2, (double) height / 2);

        // マレットの初期配置
        mallet = new Mallet((double) width / 2, (double) height * 3 / 4);

        // タイマーの初期化
        // 60FPS
        timer = new Timer(8, e -> {
            update();
            repaint();
        });

        timer.start();

        this.addMouseMotionListener(this);

        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    // update
    void update() {
        puck.update(mallet, width, height);
    }

    // draw
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        field.draw(g2d);
        puck.draw(g2d);
        mallet.draw(g2d);
    }


    // Mouse
    @Override
    public void mouseMoved(MouseEvent e) {
        mallet.setPosition(e.getX(), e.getY());
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}

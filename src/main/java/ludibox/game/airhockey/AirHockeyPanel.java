package ludibox.game.airhockey;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;

import javax.swing.*;
import java.awt.*;

public class AirHockeyPanel extends GamePanel {

    // タイマー
    private Timer timer;

    private Puck puck;  // パック

    public AirHockeyPanel(MainWindow m) {
        super(m);

        init();
    }

    // 初期化処理
    void init() {
        // パックの初期配置
        puck = new Puck((double) window.getWidth() / 2, (double) window.getHeight() / 2);

        // タイマーの初期化
        timer = new Timer(16, e -> {
            update();
            repaint();
        });
    }

    // update
    void update() {
        puck.update();
    }

    // draw
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        puck.draw(g2d);
    }
}

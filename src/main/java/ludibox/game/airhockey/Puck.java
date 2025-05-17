package ludibox.game.airhockey;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Puckクラス
 * - AirHockey
 */
public class Puck {
    private double x, y; // 中心座標
    private double vx, vy; // 速度ベクトル
    private final double radius = 20;

    public Puck(double startX, double startY) {
        this.x = startX; this.y = startY;
        this.vx = 0; this.vy = 0;
    }

    // update
    public void update() {
        x += vx;
        y += vy;

        // 壁の反射判定
        if (x - radius < 0 || x + radius > 800/*仮置き*/) vx = -vx;
        if (y - radius < 0 || y + radius > 600/*仮置き*/) vy = -vy;
    }

    // draw
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        Ellipse2D.Double circle = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
        g2d.fill(circle);
    }
}

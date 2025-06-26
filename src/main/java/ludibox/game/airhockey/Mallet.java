package ludibox.game.airhockey;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * マレット
 * - AirHockey
 */
public class Mallet {
    private double x, y;                // 現在位置
    private double prevX, prevY;        // 前回の位置
    private final double radius = 30;   // 半径
    private final Color color;          // カラー

    /* コンストラクタ */
    public Mallet(double x, double y, Color color) {
        // 初期位置を設定
        this.x = x; this.y = y;
        this.prevX = x; this.prevY = y;
        this.color = color;
    }

    // 前回位置の更新
    public void updatePrevPos() {
        prevX = x; prevY = y;
    }

    // draw
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        Ellipse2D.Double circle = new Ellipse2D.Double(
            x - radius, y - radius, radius * 2, radius * 2
        );
        g2d.fill(circle);
    }

    // 移動
    public void setPos(double dx, double dy) {
        x = dx; y = dy;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRad() { return radius; }
    public double getVelocityX() { return x - prevX; }
    public double getVelocityY() { return y - prevY; }
}

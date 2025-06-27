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

    // draw
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        Ellipse2D.Double circle = new Ellipse2D.Double(
                x - radius, y - radius, radius * 2, radius * 2
        );
        g2d.fill(circle);
    }

    // 移動
    public void setPos(double dx, double dy, Rectangle bounds) {
        prevX = x; prevY = y;   // 前回位置の保持
        x = dx; y = dy;
        // 範囲外に出ないように制限
        x = Math.max(bounds.x + radius, Math.min(x, bounds.x + bounds.width - radius));
        y = Math.max(bounds.y + bounds.height / 2. + radius, Math.min(y, bounds.y + bounds.height - radius));
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRad() { return radius; }
    public double getVx() { return x - prevX; }
    public double getVy() { return y - prevY; }
}
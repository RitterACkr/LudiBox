package ludibox.game.airhockey;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * マレット
 * - AirHockey
 */
public class Mallet {

    private double x, y;
    private final double radius = 30;

    /* コンストラクタ */
    public Mallet(double x, double y) {
        // 初期位置を設定
        this.x = x; this.y = y;
    }

    // 位置の設定
    public void setPosition(double x, double y) {
        this.x = x; this.y = y;
    }

    // draw
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        Ellipse2D.Double circle = new Ellipse2D.Double(
            x - radius, y - radius, radius * 2, radius * 2
        );
        g2d.fill(circle);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
}

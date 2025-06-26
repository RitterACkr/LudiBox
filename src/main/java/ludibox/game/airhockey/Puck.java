package ludibox.game.airhockey;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Puckクラス
 * - AirHockey
 */
public class Puck {
    private double x, y;                // 中心座標
    private double vx, vy;              // 速度ベクトル
    private final double radius = 20;   // 半径
    private final Color color;          // カラー

    public Puck(double startX, double startY, Color color) {
        this.x = startX; this.y = startY - radius;
        this.vx = 0; this.vy = 0;
        this.color = color;
    }

    // 速度の適用
    public void applyVelocity(double vx, double vy) {
        this.vx = vx; this.vy = vy;
    }

    // 位置の設定
    public void setPos(double dx, double dy) {
        this.x += dx; this.y += dy;
    }

    // update
    public void update(Rectangle bounds) {
        // 位置の更新
        x += vx; y += vy;

        // 速度の減少
        vx *= 0.99; vy *= 0.99;

        // 壁の衝突判定
        if (x - radius < bounds.x || x + radius > bounds.x + bounds.width) {
            vx *= -1;
            x = Math.max(radius, Math.min(x, bounds.x + bounds.width - radius));
        }
        if (y - radius < bounds.y || y + radius > bounds.y + bounds.height) {
            vy *= -1;
            y = Math.max(radius, Math.min(y, bounds.y + bounds.height - radius));
        }
    }

    // draw
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        Ellipse2D.Double circle = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
        g2d.fill(circle);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRad() { return radius; }

//    if (distance < minDistance && distance != 0) {
//        // 速度の更新
//        vx = mal.getVelocityX();
//        vy = mal.getVelocityY();
//
//        double overlap = minDistance - distance;
//        double nx = dx / distance;
//        double ny = dy / distance;
//        x += nx * overlap;
//        y += ny * overlap;
//
//        // ベクトルの反射
//        double dot = vx * nx + vy * ny;
//        vx = vx - 2 * dot * nx;
//        vy = vy - 2 * dot * ny;
//    }
}

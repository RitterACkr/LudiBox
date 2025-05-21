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
        this.x = startX; this.y = startY - radius;
        this.vx = 0; this.vy = 0;
    }

    public void checkCollisionMallet(Mallet mal) {
        double dx = x - mal.getX();
        double dy = y - mal.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = radius + mal.getRadius();

        if (distance < minDistance && distance != 0) {
            double overlap = minDistance - distance;
            double nx = dx / distance;
            double ny = dy / distance;
            x += nx * overlap;
            y += ny * overlap;

            // ベクトルの反射
            double dot = vx * nx + vy * ny;
            vx = vx - 2 * dot * nx;
            vy = vy - 2 * dot * ny;
        }
    }

    // update
    public void update(Mallet mal, int width, int height) {
        x += vx;
        y += vy;

        checkCollisionMallet(mal);

        // 壁の反射判定
        if (x - radius < 0 || x + radius > width) vx = -vx;
        if (y - radius < 0 || y + radius > height) vy = -vy;
    }

    // draw
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        Ellipse2D.Double circle = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
        g2d.fill(circle);
    }
}

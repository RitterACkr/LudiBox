package ludibox.game.airhockey;

import java.awt.*;

/**
 * エアホッケーのフィールド
 * - AirHockey
 */
public class Field {
    private Rectangle bounds;

    private final Mallet player;
    private final Puck puck;

    public Field(int panelWidth, int panelHeight ,int width, int height) {
        int offsetX = (panelWidth - width) / 2;
        int offsetY = (panelHeight - height) / 2;
        this.bounds = new Rectangle(offsetX, offsetY, width, height);

        this.player = new Mallet(panelWidth / 2., height * 3. / 4., Color.RED);
        this.puck = new Puck(panelWidth / 2., panelHeight / 2., Color.BLACK);
    }

    // update
    public void update() {
        // 衝突判定
        double dx = puck.getX() - player.getX();
        double dy = puck.getY() - player.getY();
        double dist = dx * dx + dy * dy;
        double minDist = puck.getRad() + player.getRad();

        if (dist < minDist * minDist) {
            double len = Math.sqrt(dist);

            // 接触法線ベクトル
            double nx = dx / len;
            double ny = dy / len;

            double mvx = player.getVelocityX();
            double mvy = player.getVelocityY();

            // 法線方向
            double impact = mvx * nx + mvy * ny;

            // めり込み補正
            double overlap = minDist - Math.sqrt(dist);
            puck.setPos(nx * overlap, ny * overlap);

            if (len > 0.1) {
                double scale = 12.; // 強さの係数
                double vx = nx * impact * scale;
                double vy = ny * impact * scale;

                puck.applyVelocity(vx, vy);
            }
        }

        puck.update(bounds);
    }

    // Playerの位置更新
    public void movePlayer(double dx, double dy) {
        player.updatePrevPos();
        player.setPos(dx, dy);
    }


    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.draw(bounds);
        g2d.drawLine(bounds.x, bounds.height / 2, bounds.x + bounds.width, bounds.height / 2);

        puck.draw(g2d);
        player.draw(g2d);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean contains(double x, double y) {
        return bounds.contains(x, y);
    }
}

package ludibox.game.airhockey;

import java.awt.*;

/**
 * エアホッケーのフィールド
 * - AirHockey
 */
public class Field {
    private Rectangle bounds;

    public Field(int panelWidth, int panelHeight ,int width, int height) {
        int offsetX = (panelWidth - width) / 2;
        int offsetY = (panelHeight - height) / 2;
        this.bounds = new Rectangle(offsetX, offsetY, width, height);
    }

    public void draw(Graphics2D g2d) {
        g2d.draw(bounds);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean contains(double x, double y) {
        return bounds.contains(x, y);
    }
}

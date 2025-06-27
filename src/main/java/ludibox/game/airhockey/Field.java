package ludibox.game.airhockey;

import java.awt.*;

/**
 * エアホッケーのフィールド
 * - AirHockey
 */
public class Field {
    private Rectangle bounds;

    private final Mallet player;
    private final Mallet aiMal;
    private final Puck puck;

    public Field(int panelWidth, int panelHeight ,int width, int height) {
        int fieldHeight = (int) (height * .99);
        int offsetX = (panelWidth - width) / 2;
        int offsetY = (panelHeight - fieldHeight) / 2;
        this.bounds = new Rectangle(offsetX, offsetY, width, fieldHeight);

        this.player = new Mallet(panelWidth / 2., height * 3. / 4., Color.RED);
        this.aiMal = new Mallet(panelWidth / 2., height / 4. , Color.BLUE);
        this.puck = new Puck(panelWidth / 2., panelHeight / 1.8, Color.BLACK);
    }

    // update
    public void update() {
        // AIマレットの移動
        updateAI();

        // Playerとpuckの判定
        checkCollision(player);
        // AIとpuckの判定
        checkCollision(aiMal);

        puck.update(bounds);
    }

    // Playerの位置更新
    public void movePlayer(double dx, double dy) {
        player.setPos(dx, dy);
        player.clampPlayerMallet(bounds);
    }

    // 描画
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.draw(bounds);
        g2d.drawLine(bounds.x, bounds.y + (bounds.height / 2), bounds.x + bounds.width, bounds.y + (bounds.height / 2));

        puck.draw(g2d);
        player.draw(g2d);
        aiMal.draw(g2d);
    }

    // マレットとの衝突判定
    public void checkCollision(Mallet mal) {
        // 衝突判定
        double dx = puck.getX() - mal.getX();
        double dy = puck.getY() - mal.getY();
        double dist = dx * dx + dy * dy;
        double minDist = puck.getRad() + mal.getRad();

        if (dist < minDist * minDist) {
            // 1. 押し出し
            double len = Math.sqrt(dist);
            // 接触法線ベクトル
            double nx = dx / len;
            double ny = dy / len;
            // マレットの速度ベクトル
            double mvx = mal.getVx();
            double mvy = mal.getVy();
            // 法線方向
            double impact = mvx * nx + mvy * ny;

            if (len > 0.1) {
                double scale = 12.; // 強さの係数
                double vx = nx * impact * scale;
                double vy = ny * impact * scale;

                puck.applyVelocity(vx, vy);
            }

            // 2. 反射処理
            // パックの速度ベクトル
            double pvx = puck.getVx();
            double pvy = puck.getVy();
            // 相対速度ベクトル
            double rvx = pvx - mvx;
            double rvy = pvy - mvy;
            // 法線方向の相対速度
            double reImpact = rvx * nx + rvy * ny;

            // パックがマレットに向かっているときのみ反発
            if (reImpact < -.1) {
                double speed = Math.sqrt(pvx * pvx + pvy * pvy);
                double bounceFactor = Math.max(1., Math.min(1.8, speed * .4));
                // 小さすぎる場合 >>> 最小値
                if (Math.abs(reImpact) < .5) {
                    reImpact = -.5 * Math.signum(reImpact);
                }
                double fx = -nx * reImpact * bounceFactor;
                double fy = -ny * reImpact * bounceFactor;

                double lateralRatio = Math.abs(fx / fy);
                if (lateralRatio > 2.) fx *= .5;

                // パックの速度に反射ベクトルを加える
                puck.applyVelocity(pvx + fx, pvy + fy);
            }

            // めり込み補正
            double overlap = minDist - Math.sqrt(dist);
            puck.setPos(nx * overlap, ny * overlap);
        }
    }

    // AIマレットの移動
    public void updateAI() {
        double speed = 12.;

        // シンプルにX軸移動
        double targetX = puck.getX();
        double dx = targetX - aiMal.getX();

        if (Math.abs(dx) > speed) {
            dx = speed * Math.signum(dx);
        }

        aiMal.setPos(aiMal.getX() + dx, aiMal.getY());
        aiMal.clampAIMallet(bounds);
    }
}
package ludibox.game.yahtzee;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;
import ludibox.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class YahtzeePanel extends GamePanel {

    // 画像バッファ
    private Image[] diceImages = new Image[6];

    private Dice[] dices = new Dice[5];
    private JButton rollButton;

    public YahtzeePanel(MainWindow m) {
        super(m);
        init();
    }

    /* 初期化 */
    public void init() {
        this.removeAll();
        this.revalidate();
        this.repaint();

        this.setLayout(null);
        this.setBackground(Color.LIGHT_GRAY);

        // 画像の読み込み
        for (int i = 0; i < 6; i++) {
            diceImages[i] = ImageLoader.loadImage("yahtzee/dice" + (i+1) + ".png");
        }

        // サイコロの初期化
        for (int i = 0; i < dices.length; i++) {
            Dice d = new Dice(diceImages);
            d.setBounds(20 + i * 70, 50, 60, 60);
            this.add(d);
            d.roll();
            dices[i] = d;
        }

        rollButton = new JButton("ROLL");
        rollButton.setBounds(20, 200, 100, 40);
        rollButton.addActionListener(e -> {
            for (Dice d : dices) d.roll();
            repaint();
        });
        this.add(rollButton);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    /* Diceクラス */
    private class Dice extends JButton  {
        private int value = 1;
        private boolean locked = false;
        private Image[] buffer;

        // アニメーション
        private Timer rollTimer;
        private int animationFrame = 0;
        private double scale = 1.0;

        public Dice(Image[] buffer) {
            this.buffer = buffer;
            this.setPreferredSize(new Dimension(60, 60));
            this.setContentAreaFilled(false);
            this.setBorderPainted(false);
            this.setFocusPainted(false);

            this.addActionListener(e -> {
                toggleLock();
                repaint();
            });
        }

        public void roll() {
            if (locked) return;

            if (rollTimer != null && rollTimer.isRunning()) {
                rollTimer.stop();
            }

            animationFrame = 0;
            rollTimer = new Timer(50, e -> {
                value = 1 + new Random().nextInt(6);

                int totalFrames = 10;
                double maxScale = 1.2;

                double progress = (double) animationFrame / totalFrames;
                scale = 1.0 + Math.sin(progress * Math.PI) * (maxScale - 1.0);

                repaint();
                animationFrame++;

                if (animationFrame >= 10) {
                    ((Timer) e.getSource()).stop();
                    value = 1 + new Random().nextInt(6);
                    scale = 1.0;
                    repaint();
                }
            });

            rollTimer.start();
        }

        public void toggleLock() {
            locked = !locked;
        }

        public boolean isLocked() { return locked; }
        public int getValue() { return value; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g.create();

            // スケーリングの中心
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            g2d.translate(cx, cy);
            g2d.scale(scale, scale);
            g2d.translate(-cx, -cy);

            if (diceImages != null && value >= 1 && value <= 6) {
                g2d.drawImage(diceImages[value - 1], 0, 0, getWidth(), getHeight(), this);
            }

            if (locked) {
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            g2d.dispose();
        }
    }
}

package ludibox.game.yahtzee;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;

import java.awt.*;
import java.util.Random;

public class YahtzeePanel extends GamePanel {

    public YahtzeePanel(MainWindow m) {
        super(m);
        init();
    }

    /* UIの初期化 */
    public void init() {
        this.removeAll();
        this.revalidate();
        this.repaint();

        this.setBackground(Color.LIGHT_GRAY);
    }


    /* Diceクラス */
    private class Dice {
        private int value;
        private boolean locked;

        public void roll() {
            if (!locked) value = 1 + new Random().nextInt(6);
        }
    }
}

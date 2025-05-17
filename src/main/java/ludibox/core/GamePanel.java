package ludibox.core;

import javax.swing.*;

// ゲームを管理する親パネル
public class GamePanel extends JPanel {

    MainWindow mainWindow; // MainWindow

    /* コンストラクタ */
    public GamePanel(MainWindow m) {
        this.mainWindow = m;
    }

    void gameFinish() {
    }
}

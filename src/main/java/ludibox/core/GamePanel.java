package ludibox.core;

import javax.swing.*;

// ゲームを管理する親パネル
public class GamePanel extends JPanel {

    protected MainWindow window; // MainWindow

    /* コンストラクタ */
    public GamePanel(MainWindow m) {
        this.window = m;
    }
}

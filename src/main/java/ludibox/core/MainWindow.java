package ludibox.core;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

// メインウィンドウクラス
public class MainWindow extends JFrame {
    @Serial
    private static final long serialVersionUID = 1L;

    // ウィンドウサイズ
    final int WIDTH = 800;
    final int HEIGHT = 600;

    // レイアウト
    CardLayout layout = new CardLayout();

    ScreenMode screenMode;  // 現在のスクリーンモード

    // パネル群
    private MenuPanel menuPanel;
    private GamePanel currentGamePanel;

    /* コンストラクタ */
    public MainWindow() {
        // ウィンドウの基本設定
        this.setTitle("Ludi Box");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(layout);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.pack();
        this.setLocationRelativeTo(null);

        // パネル
        preparePanels();

        // 表示
        this.setVisible(true);
    }

    // パネルの生成
    void preparePanels() {
        // MenuPanel
        menuPanel = new MenuPanel(this);
        this.add(menuPanel, "MENU");
        this.pack();
    }

    // スクリーンモードの切り替え
    public void switchScreen(ScreenMode s) {
        screenMode = s;

        switch (screenMode) {
            case MENU -> {
                layout.show(this.getContentPane(), "MENU");
                menuPanel.requestFocus();
            }
            case GAME -> {
                layout.show(this.getContentPane(), "GAME");
                currentGamePanel.requestFocus();
            }
            default -> {}
        }
    }

    // ゲームの開始
    public void startGame(MiniGame game, GameSetupPanel setupPanel) {
        // 既存のGamePanelを削除
        if (currentGamePanel != null) {
            this.getContentPane().remove(currentGamePanel);
        }
        // 新しいGamePanelの作成
        currentGamePanel = GameFactory.create(this, game, setupPanel);
        this.getContentPane().add(currentGamePanel, "GAME");

        // 表示切替
        switchScreen(ScreenMode.GAME);
    }
}

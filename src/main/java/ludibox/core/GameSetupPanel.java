package ludibox.core;

import javax.swing.*;

public interface GameSetupPanel {
    // 設定完了時のコールバック
    void setOnStart(Runnable onStart);

    // キャンセル時のコールバック
    void setOnCancel(Runnable onCancel);

    // パネル本体の取得
    JPanel getPanel();

    // ゲームの取得
    MiniGame getGame();
}

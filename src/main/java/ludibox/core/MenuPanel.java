package ludibox.core;

import ludibox.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

// メインメニュー画面
public class MenuPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final MainWindow mainWindow; // MainWindow

    /* コンストラクタ */
    public MenuPanel(MainWindow m) {
        this.mainWindow = m;

        this.setLayout(new BorderLayout());

        // UI
        prepareComponents();
    }

    /* メソッド - コンポーネントの準備 */
    void prepareComponents() {
        // ボタンを配置するパネル
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // ボタンの生成
        int row = 0, col = 0;
        for (MiniGame game : MiniGame.values()) {
            MenuButton button = new MenuButton(game);

            // 配置位置
            gbc.gridx = col;
            gbc.gridy = row;

            buttonPanel.add(button, gbc);

            col++;
            if (col >= 2) {
                col = 0; row++;
            }
        }

        // ボタン数が奇数ならダミー追加
        if (MiniGame.values().length % 2 != 0) {
            gbc.gridx = 1;
            gbc.gridy = row;
            buttonPanel.add(Box.createGlue(), gbc);
        }

        // スクロール可能にする
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane, BorderLayout.CENTER);
    }


    /**
     * インナークラス - メニューボタン
     */
    private class MenuButton extends JButton {

        MenuButton(MiniGame game) {
            super(game.getName());

            // 高さ制限
            int maxHeight = 200;
            this.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
            this.setPreferredSize(new Dimension(0, maxHeight));

            // 文字の位置を左下に設定
            this.setHorizontalAlignment(SwingConstants.LEFT);
            this.setVerticalAlignment(SwingConstants.BOTTOM);

            // 文字の設定
            this.setForeground(Color.WHITE);
            this.setFont(new Font("Mono", Font.BOLD, 36));

            // 背景色の変更
            this.setBackground(new Color(80, 80, 80));
            this.setOpaque(true);

            this.addActionListener(e -> {
                mainWindow.startGame(game);
            });
        }
    }
}

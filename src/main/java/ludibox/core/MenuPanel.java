package ludibox.core;

import ludibox.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.Objects;

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
            button.addActionListener(e -> showSetupPopup(game));

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

    /* セットアップの呼び出し */
    private void showSetupPopup(MiniGame game) {
        GameSetupPanel setupPanel = GameFactory.createSetup(game);

        // オプション設定がなければこのメソッドを中断してゲームを起動
        if (setupPanel == null) {
            mainWindow.startGame(game, null);
            return;
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this));

        dialog.setUndecorated(true);
        dialog.setModal(true);
        dialog.getContentPane().add(setupPanel.getPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(null);

        setupPanel.setOnStart(() -> {
            mainWindow.startGame(game, setupPanel);
            dialog.dispose();
            remove(dialog);
            revalidate();
            repaint();
        });
        setupPanel.setOnCancel(() -> {
            dialog.dispose();
            remove(dialog);
            revalidate();
            repaint();
        });

        dialog.setVisible(true);
    }

    /**
     * インナークラス - メニューボタン
     */
    private class MenuButton extends JButton {
        private Image backgroundImage;

        MenuButton(MiniGame game) {
            super(game.getName());

            try {
                backgroundImage = ImageLoader.loadImage("bgimg/" + game.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }

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

            // ボタンの反応
            this.setFocusPainted(false);

            // 背景色の変更
            this.setOpaque(false);
            this.setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }

            int textHeight = getFontMetrics(getFont()).getHeight();
            int rectY = getHeight() - textHeight - 10;
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, rectY, getWidth(), textHeight + 10);

            g2d.dispose();
            super.paintComponent(g);
        }
    }
}

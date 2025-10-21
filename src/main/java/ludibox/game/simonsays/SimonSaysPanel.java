package ludibox.game.simonsays;

import ludibox.core.GamePanel;
import ludibox.core.MainWindow;

import javax.swing.*;
import java.awt.*;

public class SimonSaysPanel extends GamePanel {

    // ゲーム状態
    private enum State {
        SHOW, INPUT, FINISH
    }

    // UI
    private final JButton[] buttons = new JButton[9];


    public SimonSaysPanel(MainWindow m) {
        super(m);

        this.setLayout(new BorderLayout());
        this.setBackground(Color.DARK_GRAY);
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        prepareComponents();
    }

    private void prepareComponents() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 10, 10));

        for (int i = 0; i < 9; i++) {
            JButton button = new JButton();
            button.setBackground(Color.WHITE);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(10, 10));

            button.addActionListener(e -> {
                button.setBackground(getBackground().darker());
                new Timer(150, ev -> {
                    button.setBackground(Color.WHITE);
                }).start();
            });

            buttons[i] = button;
            buttonPanel.add(button);
        }

        this.add(buttonPanel, BorderLayout.CENTER);

    }
}

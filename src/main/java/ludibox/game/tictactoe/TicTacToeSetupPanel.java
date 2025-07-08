package ludibox.game.tictactoe;

import ludibox.core.GameSetupPanel;
import ludibox.core.MiniGame;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;

import javax.swing.*;
import java.awt.*;

public class TicTacToeSetupPanel implements GameSetupPanel {
    private final JPanel panel;
    JComboBox<String> comboBox;

    private Runnable onStart;
    private Runnable onCancel;

    public TicTacToeSetupPanel() {
        panel = new JPanel();
        panel.setPreferredSize(new Dimension(500, 300));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Tic Tac Toe");
        title.setFont(new Font("Arial", Font.BOLD, 48));

        JLabel label = new JLabel("AIの強さを選んでください");
        label.setFont(new Font("Mono", Font.BOLD, 16));

        String[] options = {"Lv.1 (ランダム)", "Lv.2 (ルールベース)", "Lv.3 (未実装)"};
        comboBox = new JComboBox<>(options);

        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboBox.setPreferredSize(new Dimension(250, 30));
        JPanel comboWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        comboWrapper.setOpaque(false);
        comboWrapper.add(comboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        CustomButton startButton = new CustomButton("Start", CustomButtonStyle.SIMPLE);
        startButton.addActionListener(e -> {
            if (onStart != null) onStart.run();
        });
        CustomButton cancelButton = new CustomButton("Cancel", CustomButtonStyle.SIMPLE);
        cancelButton.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
        });
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);

        panel.add(Box.createVerticalStrut(10));
        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(comboWrapper);
        panel.add(Box.createVerticalStrut(50));
        panel.add(buttonPanel);
    }

    public int getLevel() {
        return comboBox.getSelectedIndex();
    }

    @Override
    public void setOnStart(Runnable onStart) {
        this.onStart = onStart;
    }

    @Override
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    @Override
    public JPanel getPanel() { return panel; }
    @Override
    public MiniGame getGame() { return MiniGame.TIC_TAC_TOE; }
}

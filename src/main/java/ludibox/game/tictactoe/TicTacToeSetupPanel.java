package ludibox.game.tictactoe;

import ludibox.core.GameSetupPanel;
import ludibox.core.MiniGame;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Flow;

public class TicTacToeSetupPanel implements GameSetupPanel {
    // Layout
    private final JPanel panel;
    private final CardLayout layout;

    // モード選択
    private JPanel modePanel;
    private boolean isVsAiMode = true;

    // AI LEVEL
    private JPanel levelPanel;
    private JComboBox<String> comboBox;

    private Runnable onStart;
    private Runnable onCancel;

    public TicTacToeSetupPanel() {
        layout = new CardLayout();
        panel = new JPanel(layout);

        panel.setPreferredSize(new Dimension(500, 300));
        panel.setBackground(Color.LIGHT_GRAY);

        createModePanel();
        createLevelPanel();

        panel.add(modePanel, "MODE");
        panel.add(levelPanel, "LEVEL");

        layout.show(panel, "MODE");
    }

    private void createModePanel() {
        modePanel = new JPanel();
        modePanel.setOpaque(false);
        modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Tic Tac Toe");
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel modeLabel = new JLabel("モードを選択してください");
        modeLabel.setFont(new Font("Mono", Font.BOLD, 16));
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonRow.setOpaque(false);

        CustomButton vsAiButton = new CustomButton("vs AI", CustomButtonStyle.SIMPLE);
        vsAiButton.addActionListener(e -> {
            isVsAiMode = true;
            layout.show(panel, "LEVEL");
        });

        CustomButton twoPlayerButton = new CustomButton("2 Player", CustomButtonStyle.SIMPLE);
        twoPlayerButton.addActionListener(e -> {
            isVsAiMode = false;
            if (onStart != null) onStart.run();
        });

        buttonRow.add(vsAiButton);
        buttonRow.add(twoPlayerButton);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomRow.setOpaque(false);
        CustomButton cancelButton = new CustomButton("Cancel", CustomButtonStyle.SIMPLE);
        cancelButton.addActionListener(e -> { if (onCancel != null) onCancel.run(); });
        bottomRow.add(cancelButton);

        modePanel.add(Box.createVerticalStrut(10));
        modePanel.add(title);
        modePanel.add(Box.createVerticalStrut(30));
        modePanel.add(modeLabel);
        modePanel.add(Box.createVerticalStrut(10));
        modePanel.add(buttonRow);
        modePanel.add(Box.createVerticalStrut(40));
        modePanel.add(bottomRow);
    }

    private void createLevelPanel() {
        levelPanel = new JPanel();
        levelPanel.setOpaque(false);
        levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("AIの強さを選択してください");
        title.setFont(new Font("Mono", Font.BOLD, 36));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] options = {"Lv.1 (ランダム)", "Lv.2 (ルールベース)", "Lv.3 (ミニマックス)"};
        comboBox = new JComboBox<>(options);
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboBox.setMaximumSize(new Dimension(260, 30));

        JPanel comboWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        comboWrapper.setOpaque(false);
        comboWrapper.add(comboBox);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 10));
        buttons.setOpaque(false);

        CustomButton backButton = new CustomButton("Back", CustomButtonStyle.SIMPLE);
        backButton.addActionListener(e -> layout.show(panel, "MODE"));

        CustomButton startButton = new CustomButton("Start", CustomButtonStyle.SIMPLE);
        startButton.addActionListener(e -> {
            isVsAiMode = true;
            if (onStart != null) onStart.run();
        });

        CustomButton cancelButton = new CustomButton("Cancel", CustomButtonStyle.SIMPLE);
        cancelButton.addActionListener(e -> { if (onCancel != null) onCancel.run(); });

        buttons.add(backButton);
        buttons.add(startButton);
        buttons.add(cancelButton);

        levelPanel.add(Box.createVerticalStrut(20));
        levelPanel.add(title);
        levelPanel.add(Box.createVerticalStrut(20));
        levelPanel.add(comboWrapper);
        levelPanel.add(Box.createVerticalStrut(30));
        levelPanel.add(buttons);
    }

    private void setAiLevelVisible(boolean visible) {
        comboBox.setEnabled(visible);
        comboBox.setVisible(visible);
    }

    public boolean isVsAiMode() {
        return isVsAiMode;
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

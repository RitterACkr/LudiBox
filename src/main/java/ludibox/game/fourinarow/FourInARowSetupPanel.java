package ludibox.game.fourinarow;

import ludibox.core.GameSetupPanel;
import ludibox.core.MiniGame;
import ludibox.ui.CustomButton;
import ludibox.ui.CustomButtonStyle;

import javax.swing.*;
import java.awt.*;

public class FourInARowSetupPanel implements GameSetupPanel {

    private JPanel panel;

    // Layout
    private JRadioButton twoPlayerRadio;
    private JRadioButton aiRadio;
    private JComboBox<String> aiLevelBox;

    private Runnable onStart;
    private Runnable onCancel;

    // モード
    private boolean vsAI = false;

    public FourInARowSetupPanel() {
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(500, 300));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // タイトル
        JLabel title = new JLabel("Four in a Row", SwingConstants.CENTER);
        title.setFont(new Font("Mono", Font.BOLD, 32));
        gbc.gridy = 0;
        panel.add(title, gbc);

        // モード選択
        twoPlayerRadio = new JRadioButton("2 Player");
        twoPlayerRadio.setOpaque(false);
        twoPlayerRadio.setSelected(true);

        aiRadio = new JRadioButton("VS AI");
        aiRadio.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(twoPlayerRadio);
        group.add(aiRadio);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        modePanel.setOpaque(false);
        modePanel.add(twoPlayerRadio);
        modePanel.add(aiRadio);

        gbc.gridy = 1;
        panel.add(modePanel, gbc);

        // AIレベル
        aiLevelBox = new JComboBox<>(new String[] {"Easy", "Normal", "Hard"});
        aiLevelBox.setVisible(false);
        gbc.gridy = 2;
        panel.add(aiLevelBox, gbc);

        // ボタンパネル
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        CustomButton startButton = new CustomButton("Start", CustomButtonStyle.SIMPLE);
        CustomButton cancelButton = new CustomButton("Cancel", CustomButtonStyle.SIMPLE);

        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 3;
        panel.add(buttonPanel, gbc);


        // イベント
        aiRadio.addActionListener(e -> aiLevelBox.setVisible(true));
        twoPlayerRadio.addActionListener(e -> aiLevelBox.setVisible(false));

        startButton.addActionListener(e -> {
            vsAI = aiRadio.isSelected();
            if (onStart != null) onStart.run();
        });

        cancelButton.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
        });
    }

    public boolean isVsAI() {
        return vsAI;
    }

    public int getSelectedLevel() {
        return aiLevelBox.getSelectedIndex();
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
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public MiniGame getGame() {
        return MiniGame.FOUR_IN_A_ROW;
    }
}

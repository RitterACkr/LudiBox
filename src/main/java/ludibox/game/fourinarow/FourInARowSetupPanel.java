package ludibox.game.fourinarow;

import ludibox.core.GameSetupPanel;
import ludibox.core.MiniGame;

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

    public FourInARowSetupPanel() {
        panel = new JPanel(new GridBagLayout());

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

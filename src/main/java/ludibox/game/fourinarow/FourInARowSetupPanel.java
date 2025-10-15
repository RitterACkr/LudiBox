package ludibox.game.fourinarow;

import ludibox.core.GameSetupPanel;
import ludibox.core.MiniGame;

import javax.swing.*;

public class FourInARowSetupPanel implements GameSetupPanel {

    // Layout
    private JRadioButton twoPlayerRadio;
    private JRadioButton aiRadio;
    private JComboBox<String> aiLevelBox;

    private Runnable onStart;
    private Runnable onCancel;


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
        return null;
    }

    @Override
    public MiniGame getGame() {
        return null;
    }
}

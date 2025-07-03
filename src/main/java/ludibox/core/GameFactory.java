package ludibox.core;

import ludibox.game.tictactoe.TicTacToePanel;
import ludibox.game.tictactoe.TicTacToeSetupPanel;

public class GameFactory {

    public static GamePanel create(MainWindow window, MiniGame game, GameSetupPanel setup) {
        return switch (game) {
            case TIC_TAC_TOE -> {
                int level = ((TicTacToeSetupPanel) setup).getLevel();
                yield new TicTacToePanel(window, level);
            }
        };
    }

    public static GameSetupPanel createSetup(MiniGame game) {
        return switch (game) {
            case TIC_TAC_TOE -> new TicTacToeSetupPanel();
        };
    }
}

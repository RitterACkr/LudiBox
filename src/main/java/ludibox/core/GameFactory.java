package ludibox.core;

import ludibox.game.fourinarow.FourInARowPanel;
import ludibox.game.fourinarow.FourInARowSetupPanel;
import ludibox.game.snakegame.SnakeGamePanel;
import ludibox.game.tictactoe.TicTacToePanel;
import ludibox.game.tictactoe.TicTacToeSetupPanel;
import ludibox.game.yahtzee.YahtzeePanel;

public class GameFactory {

    public static GamePanel create(MainWindow window, MiniGame game, GameSetupPanel setup) {
        return switch (game) {
            case TIC_TAC_TOE -> {
                int level = ((TicTacToeSetupPanel) setup).getLevel();
                boolean isVsAi = ((TicTacToeSetupPanel) setup).isVsAiMode();
                yield new TicTacToePanel(window, isVsAi, level);
            }
            case SNAKE_GAME -> new SnakeGamePanel(window);
            case YAHTZEE -> new YahtzeePanel(window);
            case FOUR_IN_A_ROW -> {
                int level = ((FourInARowSetupPanel) setup).getSelectedLevel();
                boolean isVsAi = ((FourInARowSetupPanel) setup).isVsAI();
                yield new FourInARowPanel(window, isVsAi, level);
            }
        };
    }

    public static GameSetupPanel createSetup(MiniGame game) {
        return switch (game) {
            case TIC_TAC_TOE -> new TicTacToeSetupPanel();
            case SNAKE_GAME -> null;
            case YAHTZEE -> null;
            case FOUR_IN_A_ROW -> new FourInARowSetupPanel();
        };
    }
}

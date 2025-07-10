package ludibox.core;

import ludibox.game.snakegame.SnakeGamePanel;
import ludibox.game.tictactoe.TicTacToePanel;
import ludibox.game.tictactoe.TicTacToeSetupPanel;
import ludibox.game.yahtzee.YahtzeePanel;

public class GameFactory {

    public static GamePanel create(MainWindow window, MiniGame game, GameSetupPanel setup) {
        return switch (game) {
            case TIC_TAC_TOE -> {
                int level = ((TicTacToeSetupPanel) setup).getLevel();
                yield new TicTacToePanel(window, level);
            }
            case SNAKE_GAME -> new SnakeGamePanel(window);
            case YAHTZEE -> new YahtzeePanel(window);
        };
    }

    public static GameSetupPanel createSetup(MiniGame game) {
        return switch (game) {
            case TIC_TAC_TOE -> new TicTacToeSetupPanel();
            case SNAKE_GAME -> null;
            case YAHTZEE -> null;
        };
    }
}

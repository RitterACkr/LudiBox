package ludibox.core;

import ludibox.game.tictactoe.TicTacToePanel;

public class GameFactory {
    public static GamePanel createGame(MainWindow m, MiniGame game) {
        return switch (game) {
            case TIC_TAC_TOE -> new TicTacToePanel(m);
        };
    }
}

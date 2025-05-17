package ludibox.core;

import ludibox.game.*;

public class GameFactory {
    public static GamePanel createGame(MainWindow m, MiniGame game) {
        return switch (game) {
            case AIR_HOCKEY -> new AirHockeyPanel(m);
        };
    }
}

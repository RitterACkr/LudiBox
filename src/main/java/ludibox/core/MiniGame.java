package ludibox.core;

public enum MiniGame {
    TIC_TAC_TOE("Tic Tac Toe", 1, "tic_tac_toe.png"),
    SNAKE_GAME("Snake Game", 2, "snake_game.png"),
    YAHTZEE("Yahtzee", 3, "yahtzee.png"),
    FOUR_IN_A_ROW("4 in a Row", 4, "four_in_a_row.png"),
    SIMON_SAYS("Simon Says", 5, "simon_says.png");

    private final String name;
    private final int id;
    private final String path;

    MiniGame(String name, int id, String path) {
        this.name = name;
        this.id = id;
        this.path = path;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public String getPath() { return path; }
}

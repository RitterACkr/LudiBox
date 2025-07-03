package ludibox.core;

public enum MiniGame {
    TIC_TAC_TOE("Tic Tac Toe", 1, "tictactoe.png");

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

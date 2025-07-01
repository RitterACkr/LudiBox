package ludibox.core;

public enum MiniGame {
    TIC_TAC_TOE("Tic Tac Toe", 1);

    private final String name;
    private final int id;

    MiniGame(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() { return name; }
    public int getId() { return id; }
}

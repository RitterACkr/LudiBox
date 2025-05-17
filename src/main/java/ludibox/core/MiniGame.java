package ludibox.core;

public enum MiniGame {
    AIR_HOCKEY("AIR HOCKEY", 1);

    private final String name;
    private final int id;

    MiniGame(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() { return name; }
    public int getId() { return id; }
}

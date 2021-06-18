package ru.boomearo.worldlister.objects;

public class WorldPlayer {

    private final String name;

    private PlayerType type;
    private final long timeAdded;
    private final String whoAdd;

    public WorldPlayer(String name, PlayerType type, long timeAdded, String whoAdd) {
        this.name = name;
        this.type = type;
        this.timeAdded = timeAdded;
        this.whoAdd = whoAdd;
    }

    public String getName() {
        return this.name;
    }

    public PlayerType getType() {
        return this.type;
    }

    public long getTimeAdded() {
        return this.timeAdded;
    }

    public String getWhoAdd() {
        return this.whoAdd;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "(" + this.type.toString() + "/" + this.timeAdded + "/" + this.whoAdd + ")";
    }

}

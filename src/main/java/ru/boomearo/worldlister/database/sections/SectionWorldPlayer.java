package ru.boomearo.worldlister.database.sections;

import ru.boomearo.worldlister.objects.PlayerType;

public class SectionWorldPlayer {

    private final int id;
    private final String name;
    private final PlayerType type;
    private final long timeAdd;
    private final String whoAdd;

    public SectionWorldPlayer(int id, String Name, PlayerType type, long timeAdd, String whoAdd) {
        this.id = id;
        this.name = Name;
        this.type = type;
        this.timeAdd = timeAdd;
        this.whoAdd = whoAdd;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public PlayerType getType() {
        return this.type;
    }

    public long getTimeAdd() {
        return this.timeAdd;
    }

    public String getWhoAdd() {
        return this.whoAdd;
    }
}

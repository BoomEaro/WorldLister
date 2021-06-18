package ru.boomearo.worldlister.database.sections;

import ru.boomearo.worldlister.objects.PlayerType;

public class SectionWorldPlayer {

    public final int id;
    public final String name;
    public final PlayerType type;
    public final long timeAdd;
    public final String whoAdd;

    public SectionWorldPlayer(int id, String Name, PlayerType type, long timeAdd, String whoAdd) {
        this.id = id;
        this.name = Name;
        this.type = type;
        this.timeAdd = timeAdd;
        this.whoAdd = whoAdd;
    }

}

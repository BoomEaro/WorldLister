package ru.boomearo.worldlister.database.sections;

public class SectionWorldPlayer {
    public final int id;
    public final String name;
    public final String type;
    public final Long timeAdd;
    public final String whoAdd;

    public SectionWorldPlayer(int id, String Name, String type, Long timeAdd, String whoAdd) {
        this.id = id;
        this.name = Name;
        this.type = type;
        this.timeAdd = timeAdd;
        this.whoAdd = whoAdd;
    }

}

package ru.boomearo.worldlister.objects;

public enum PlayerType {
    SPECTATOR("Наблюдатель"),
    MEMBER("Участник"),
    MODER("Модератор"),
    OWNER("Владелец");

    private final String name;

    PlayerType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

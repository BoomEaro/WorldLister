package ru.boomearo.worldlister.objects;

public enum WorldAccessType {

    PUBLIC("Публичный"),
    ACCESS("Приватный");

    private final String name;

    WorldAccessType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

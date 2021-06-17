package ru.boomearo.worldlister.objects;

public enum WorldAccess {
    PUBLIC("Публичный"),
    ACCESS("Приватный");

    private final String name;

    WorldAccess(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

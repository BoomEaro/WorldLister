package ru.boomearo.worldlister.objects;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProtectedWorld {

    private final String name;

    private final ConcurrentMap<String, WorldPlayer> players = new ConcurrentHashMap<String, WorldPlayer>();

    private boolean joinIfOwnerOnline;
    private WorldAccessType access;

    public ProtectedWorld(String name, boolean joinIfOwnerOnline, WorldAccessType access) {
        this.name = name;
        this.joinIfOwnerOnline = joinIfOwnerOnline;
        this.access = access;
    }

    public String getName() {
        return this.name;
    }

    public WorldPlayer getWorldPlayer(String player) {
        return this.players.get(player);
    }

    public Collection<WorldPlayer> getAllWorldPlayers() {
        return this.players.values();
    }

    public Set<String> getAllWorldPlayersString() {
        return this.players.keySet();
    }

    public void addWorldPlayer(WorldPlayer player) {
        this.players.put(player.getName(), player);
    }

    public void removeWorldPlayer(String player) {
        this.players.remove(player);
    }

    public boolean isJoinIfOwnerOnline() {
        return this.joinIfOwnerOnline;
    }

    public WorldAccessType getAccess() {
        return this.access;
    }

    public void setJoinIfOwnerOnline(boolean enabled) {
        this.joinIfOwnerOnline = enabled;
    }

    public void setAccess(WorldAccessType type) {
        this.access = type;
    }

    @Override
    public String toString() {
        return "(" + this.players + "/" + this.joinIfOwnerOnline + "/" + this.access.name() + ")";
    }
}
	

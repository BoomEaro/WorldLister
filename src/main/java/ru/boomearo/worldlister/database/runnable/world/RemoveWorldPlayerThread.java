package ru.boomearo.worldlister.database.runnable.world;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;

public class RemoveWorldPlayerThread extends BukkitRunnable {
    private final String worldName;
    private final String playerName;

    public RemoveWorldPlayerThread(String worldName, String playerName) {
        this.worldName = worldName;
        this.playerName = playerName;
        runnable();
    }

    private void runnable() {
        this.runTaskAsynchronously(WorldLister.getInstance());
    }

    @Override
    public void run() {
        try {
            Sql.getInstance().removeWorldPlayer(worldName, playerName);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

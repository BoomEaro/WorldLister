package ru.boomearo.worldlister.database.runnable.world;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;

public class PutWorldPlayerThread extends BukkitRunnable {
    private final String worldName;
    private final String playerName;
    private final String type;
    private final Long timeAdded;
    private final String whoAdd;

    public PutWorldPlayerThread(String worldName, String playerName, String type, Long timeAdded, String whoAdd) {
        this.worldName = worldName;
        this.playerName = playerName;
        this.type = type;
        this.timeAdded = timeAdded;
        this.whoAdd = whoAdd;
        runnable();
    }

    private void runnable() {
        this.runTaskAsynchronously(WorldLister.getInstance());
    }

    @Override
    public void run() {
        try {
            Sql.getInstance().putWorldPlayer(worldName, playerName, type, timeAdded, whoAdd);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

package ru.boomearo.worldlister;

import java.io.File;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.worldlister.command.Commands;
import ru.boomearo.worldlister.database.Sql;
import ru.boomearo.worldlister.listeners.CheckListener;
import ru.boomearo.worldlister.listeners.WorldListener;
import ru.boomearo.worldlister.managers.MessageManager;
import ru.boomearo.worldlister.managers.ProtectedWorldManager;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.ProtectedWorld;
import ru.boomearo.worldlister.objects.WorldPlayer;

public class WorldLister extends JavaPlugin implements Listener {

    private ProtectedWorldManager manager = null;

    private static WorldLister instance = null;

    @Override
    public void onEnable() {
        instance = this;

        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Конфиг не найден, создаю новый..");
            saveDefaultConfig();
        }

        MessageManager.get().loadMessages();
        loadDataBase();

        if (this.manager == null) {
            this.manager = new ProtectedWorldManager();

            this.manager.loadWorlds();

            this.manager.checkPlayersAccess();
        }

        getServer().getPluginManager().registerEvents(new CheckListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);

        getCommand("worldlist").setExecutor(new Commands());

        getLogger().info("Плагин успешно запущен.");
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().disconnect();
            getLogger().info("Успешно отключился от базы данных");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        getLogger().info("Плагин успешно выключился.");
    }

    public ProtectedWorldManager getProtectedWorldManager() {
        return manager;
    }

    public static WorldLister getInstance() {
        return instance;
    }

    public void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        try {
            Sql.initSql();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkOnline(ProtectedWorld wi) {
        for (WorldPlayer wpa : wi.getAllWorldPlayers()) {
            if (wpa.getType() == PlayerType.OWNER) {
                if (getPlayerRight(wpa.getName()) != null) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void tpWorldClear(Player pl) {
        pl.getInventory().clear();
        pl.chat("//none");
    }

    public static Player getPlayerRight(String name) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getName().equals(name)) {
                return pl;
            }
        }
        return null;
    }


}

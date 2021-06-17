package ru.boomearo.worldlister;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import ru.boomearo.worldlister.command.Commands;
import ru.boomearo.worldlister.database.Sql;
import ru.boomearo.worldlister.database.runnable.settings.PutSettingsThread;
import ru.boomearo.worldlister.database.sections.SectionWorld;
import ru.boomearo.worldlister.database.sections.SectionWorldPlayer;
import ru.boomearo.worldlister.listeners.CheckListener;
import ru.boomearo.worldlister.listeners.WorldListener;
import ru.boomearo.worldlister.managers.MessageManager;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.WorldAccess;
import ru.boomearo.worldlister.objects.WorldInfo;
import ru.boomearo.worldlister.objects.WorldPlayer;

public class WorldLister extends JavaPlugin implements Listener {

    private final ConcurrentMap<String, WorldInfo> worlds = new ConcurrentHashMap<String, WorldInfo>();

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

        getServer().getPluginManager().registerEvents(new CheckListener(), this);
        getServer().getPluginManager().registerEvents(new WorldListener(), this);

        getCommand("worldlist").setExecutor(new Commands());

        loadWorlds();

        checkPlayerAccess();

        getLogger().info("Плагин успешно запущен.");
    }

    @Override
    public void onDisable() {
        try {
            getLogger().info("Отключаюсь от базы данных");
            Sql.getInstance().disconnect();
            getLogger().info("Успешно отключился от базы данных");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        getLogger().info("Плагин успешно выключился.");
    }

    public static WorldLister getInstance() {
        return instance;
    }

    public void loadDataBase() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        try {
            for (World w : Bukkit.getWorlds()) {
                Sql.getInstance().createNewDatabaseWorld(w.getName());
            }
            Sql.getInstance().createNewDatabaseSettings();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadWorlds()  {
        try {
            for (World w : Bukkit.getWorlds()) {
                SectionWorld sw = Sql.getInstance().getDataSettings(w.getName());
                if (sw != null) {
                    this.worlds.put(w.getName(), new WorldInfo(w.getName(), sw.joinIf, WorldAccess.valueOf(sw.access)));
                }
                else {
                    this.worlds.put(w.getName(), new WorldInfo(w.getName(), false, WorldAccess.PUBLIC));
                    new PutSettingsThread(w.getName(), false, "PUBLIC");
                }
            }
            for (WorldInfo wi : this.worlds.values()) {
                for (SectionWorldPlayer swp : Sql.getInstance().getAllDataWorldPlayer(wi.getName())) {
                    wi.addWorldPlayer(new WorldPlayer(swp.name, PlayerType.valueOf(swp.type), swp.timeAdd, swp.whoAdd));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkOnline(WorldInfo wi) {
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

    public void checkPlayerAccess() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            String w = pl.getLocation().getWorld().getName();
            WorldInfo wi = this.worlds.get(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            String msg = MessageManager.get().getMessage("joinPerms");
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp == null) {
                pl.teleport(Bukkit.getWorld("world").getSpawnLocation());
                pl.sendMessage(msg.replace("%WORLD%", w).replace("&", "\u00a7"));
                return;
            }
        }
    }


    public static Player getPlayerRight(String name) {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getName().equals(name)) {
                return pl;
            }
        }
        return null;
    }

    public WorldInfo getWorldInfo(String world) {
        return this.worlds.get(world);
    }

    public Collection<WorldInfo> getAllWorlds() {
        return this.worlds.values();
    }

    public void addWorldInfo(WorldInfo world) {
        this.worlds.put(world.getName(), world);
    }

    public void removeWorldInfo(String world) {
        this.worlds.remove(world);
    }

}

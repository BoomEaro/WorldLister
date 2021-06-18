package ru.boomearo.worldlister.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import ru.boomearo.worldlister.database.Sql;
import ru.boomearo.worldlister.database.sections.SectionWorld;
import ru.boomearo.worldlister.database.sections.SectionWorldPlayer;
import ru.boomearo.worldlister.objects.ProtectedWorld;
import ru.boomearo.worldlister.objects.WorldAccessType;
import ru.boomearo.worldlister.objects.WorldPlayer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ProtectedWorldManager {

    private ConcurrentMap<String, ProtectedWorld> worlds = new ConcurrentHashMap<String, ProtectedWorld>();

    public ProtectedWorld getProtectedWorld(String world) {
        return this.worlds.get(world);
    }

    public Collection<ProtectedWorld> getAllProtectedWorlds() {
        return this.worlds.values();
    }

    public void addProtectedWorld(ProtectedWorld world) {
        this.worlds.put(world.getName(), world);
    }

    public void removeProtectedWorld(String world) {
        this.worlds.remove(world);
    }

    public void loadWorlds()  {
        try {
            ConcurrentMap<String, ProtectedWorld> tmp = new ConcurrentHashMap<String, ProtectedWorld>();
            for (World w : Bukkit.getWorlds()) {
                SectionWorld sw = Sql.getInstance().getDataSettings(w.getName()).get();
                ProtectedWorld newPw;
                if (sw != null) {
                    newPw = new ProtectedWorld(w.getName(), sw.joinIf, sw.access);
                }
                else {
                    newPw = new ProtectedWorld(w.getName(), false, WorldAccessType.PUBLIC);
                    Sql.getInstance().putSettings(w.getName(), false, WorldAccessType.PUBLIC);
                }

                for (SectionWorldPlayer swp : Sql.getInstance().getAllDataWorldPlayer(newPw.getName()).get()) {
                    newPw.addWorldPlayer(new WorldPlayer(swp.name, swp.type, swp.timeAdd, swp.whoAdd));
                }

                tmp.put(w.getName(), newPw);
            }
            this.worlds = tmp;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkPlayersAccess() {
        for (Player pl : Bukkit.getOnlinePlayers()) {
            String w = pl.getLocation().getWorld().getName();
            ProtectedWorld wi = this.worlds.get(w);
            if (wi == null) {
                continue;
            }
            if (wi.getAccess() == WorldAccessType.PUBLIC) {
                continue;
            }
            String msg = MessageManager.get().getMessage("joinPerms");
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp != null) {
                continue;
            }

            pl.teleport(Bukkit.getWorld("world").getSpawnLocation());
            pl.sendMessage(msg.replace("%WORLD%", w));
        }
    }
}

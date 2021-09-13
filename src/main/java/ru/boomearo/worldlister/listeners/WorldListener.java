package ru.boomearo.worldlister.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;
import ru.boomearo.worldlister.database.sections.SectionWorld;
import ru.boomearo.worldlister.database.sections.SectionWorldPlayer;
import ru.boomearo.worldlister.managers.ProtectedWorldManager;
import ru.boomearo.worldlister.objects.WorldAccessType;
import ru.boomearo.worldlister.objects.ProtectedWorld;
import ru.boomearo.worldlister.objects.WorldPlayer;

public class WorldListener implements Listener {


    //Подгружает настройки мира, если мир был подгружен во время работы сервера (например из-за плагинов на мульти миры)
    //Все запросы будут в основном потоке, потому что миры и так подгружаются там же и всегда будет задержка.
    @EventHandler
    public void onWorldLoadEvent(WorldLoadEvent e) {
        World w = e.getWorld();
        ProtectedWorldManager manager = WorldLister.getInstance().getProtectedWorldManager();
        ProtectedWorld worldInfo = manager.getProtectedWorld(w.getName());
        if (worldInfo != null) {
            return;
        }
        try {
            Sql.getInstance().createNewDatabaseWorld(w.getName());

            SectionWorld sw = Sql.getInstance().getDataSettings(w.getName()).get();
            if (sw != null) {
                worldInfo = new ProtectedWorld(w.getName(), w, sw.joinIf, sw.access);
                manager.addProtectedWorld(worldInfo);
            }
            else {
                worldInfo = new ProtectedWorld(w.getName(), w, false, WorldAccessType.PUBLIC);
                manager.addProtectedWorld(worldInfo);

                Sql.getInstance().putSettings(w.getName(), false, WorldAccessType.PUBLIC);
            }
            for (SectionWorldPlayer swp : Sql.getInstance().getAllDataWorldPlayer(w.getName()).get()) {
                worldInfo.addWorldPlayer(new WorldPlayer(swp.name, swp.type, swp.timeAdd, swp.whoAdd));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onWorldUnloadEvent(WorldUnloadEvent e) {
        World w = e.getWorld();
        ProtectedWorldManager manager = WorldLister.getInstance().getProtectedWorldManager();
        ProtectedWorld worldInfo = manager.getProtectedWorld(w.getName());
        if (worldInfo == null) {
            return;
        }
        manager.removeProtectedWorld(w.getName());
    }

}

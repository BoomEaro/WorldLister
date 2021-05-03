package ru.boomearo.worldlister.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;
import ru.boomearo.worldlister.database.runnable.settings.PutSettingsThread;
import ru.boomearo.worldlister.database.sections.SectionWorld;
import ru.boomearo.worldlister.database.sections.SectionWorldPlayer;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.WorldAccess;
import ru.boomearo.worldlister.objects.WorldInfo;
import ru.boomearo.worldlister.objects.WorldPlayer;

public class WorldListener implements Listener {

	@EventHandler
	public void onWorldLoadEvent(WorldLoadEvent e) {
		World w = e.getWorld();
		WorldInfo worldInfo = WorldLister.getContext().getWorldInfo(w.getName());
		if (worldInfo != null) {
			return;
		}
		try {
			Sql.getInstance().createNewDatabaseWorld(w.getName());
			
			SectionWorld sw = Sql.getInstance().getDataSettings(w.getName());
			if (sw != null) {
				worldInfo = new WorldInfo(w.getName(), sw.joinIf, WorldAccess.valueOf(sw.access));
				WorldLister.getContext().addWorldInfo(worldInfo);
			}
			else {
				worldInfo = new WorldInfo(w.getName(), false, WorldAccess.PUBLIC);
				WorldLister.getContext().addWorldInfo(worldInfo);
			    new PutSettingsThread(w.getName(), false, "PUBLIC");
			}
			for (SectionWorldPlayer swp : Sql.getInstance().getAllDataWorldPlayer(w.getName())) {
				worldInfo.addWorldPlayer(new WorldPlayer(swp.name, PlayerType.valueOf(swp.type), swp.timeAdd, swp.whoAdd));
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@EventHandler
	public void onWorldUnloadEvent(WorldUnloadEvent e) {
		World w = e.getWorld();
		WorldInfo worldInfo = WorldLister.getContext().getWorldInfo(w.getName());
		if (worldInfo == null) {
			return;
		}
		WorldLister.getContext().removeWorldInfo(w.getName());
	}
	
}
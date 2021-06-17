package ru.boomearo.worldlister.database.runnable.settings;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;

public class RemoveSettingsThread extends BukkitRunnable {
	private String worldName;
	public RemoveSettingsThread(String worldName) {
		this.worldName = worldName;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(WorldLister.getContext());
	}
	

	@Override
	public void run() {
		try {
			Sql.getInstance().removeSettings(worldName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

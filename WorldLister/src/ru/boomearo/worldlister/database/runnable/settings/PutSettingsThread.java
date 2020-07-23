package ru.boomearo.worldlister.database.runnable.settings;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;

public class PutSettingsThread extends BukkitRunnable {
	private String worldName;
	private boolean isJoin;
	private String access;
	public PutSettingsThread(String worldName, boolean isJoin, String access) {
		this.worldName = worldName;
		this.isJoin = isJoin;
		this.access = access;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(WorldLister.getContext());
	}
	

	@Override
	public void run() {
		try {
			Sql.getInstance().putSettings(worldName, isJoin, access);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

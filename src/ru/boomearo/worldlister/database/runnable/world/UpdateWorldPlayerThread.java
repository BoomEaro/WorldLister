package ru.boomearo.worldlister.database.runnable.world;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;


public class UpdateWorldPlayerThread extends BukkitRunnable {
	private String worldName;
	private String playerName;
	private String type;
	private Long timeAdded;
	private String whoAdd;
	public UpdateWorldPlayerThread(String worldName, String playerName, String type, Long timeAdded, String whoAdd) {
		this.worldName = worldName;
		this.playerName = playerName;
		this.type = type;
		this.timeAdded = timeAdded;
		this.whoAdd = whoAdd;
		runnable();
	}
	
	private void runnable() {
		this.runTaskAsynchronously(WorldLister.getContext());
	}
	

	@Override
	public void run() {
		try {
			Sql.getInstance().updateWorldPlayer(worldName, playerName, type, timeAdded, whoAdd);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

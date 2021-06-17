package ru.boomearo.worldlister.database.runnable.world;

import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.Sql;

public class PutWorldPlayerThread extends BukkitRunnable {
	private String worldName;
	private String playerName;
	private String type;
	private Long timeAdded;
	private String whoAdd;
	public PutWorldPlayerThread(String worldName, String playerName, String type, Long timeAdded, String whoAdd) {
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
			Sql.getInstance().putWorldPlayer(worldName, playerName, type, timeAdded, whoAdd);
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

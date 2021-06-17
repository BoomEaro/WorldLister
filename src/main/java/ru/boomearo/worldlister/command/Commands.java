package ru.boomearo.worldlister.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.database.runnable.settings.UpdateSettingsThread;
import ru.boomearo.worldlister.database.runnable.world.PutWorldPlayerThread;
import ru.boomearo.worldlister.database.runnable.world.RemoveWorldPlayerThread;
import ru.boomearo.worldlister.database.runnable.world.UpdateWorldPlayerThread;
import ru.boomearo.worldlister.managers.MessageManager;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.WorldAccess;
import ru.boomearo.worldlister.objects.WorldInfo;
import ru.boomearo.worldlister.objects.WorldPlayer;
import ru.boomearo.worldlister.utils.DateUtil;

public class Commands implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command worldlist, String label, String[] args) {
		if (sender instanceof Player) {
			Player pl = (Player) sender;
			if (!(pl.hasPermission("worldlist.commands") || pl.getName().equals("BoomEaro"))) {
				pl.sendMessage(MessageManager.get().getMessage("dontHavePermissions").replace("&", "\u00a7"));
				return true;
			}
			if (args.length == 0) {
	    		for (String s : MessageManager.get().getMessageList("help")) {
	    			sender.sendMessage(s.replace("&", "\u00a7").replace("{version}", WorldLister.getContext().getDescription().getVersion()));
	    		}
			}
			if (args.length == 1) {
	     	   if (args[0].equalsIgnoreCase("list")) {
    			   WorldInfo wi = WorldLister.getContext().getWorldInfo(pl.getWorld().getName());
    			   if (wi != null) {
    				   List<String> owners = new ArrayList<String>();
    				   List<String> moders = new ArrayList<String>();
    				   List<String> members = new ArrayList<String>();
    				   List<String> spectators = new ArrayList<String>();
    				   for (WorldPlayer wp : wi.getAllWorldPlayers()) {
    					   if (wp.getType() == PlayerType.OWNER) {
    						   owners.add(wp.getName());
    					   }
    					   else if (wp.getType() == PlayerType.MODER) {
    						   moders.add(wp.getName());
    					   }
    					   else if (wp.getType() == PlayerType.MEMBER) {
    						   members.add(wp.getName());
    					   }
    					   else if (wp.getType() == PlayerType.SPECTATOR) {
    						   spectators.add(wp.getName());
    					   }
    				   }
            		   String[] list = members.toArray(new String[members.size()]);
            		   int len = list.length;
            		   String msg = "";
            		   
            		   String[] list2 = moders.toArray(new String[moders.size()]);
            		   int len2 = list2.length;
            		   String msg2 = "";
            		   
            		   String[] list3 = owners.toArray(new String[owners.size()]);
            		   int len3 = list3.length;
            		   String msg3 = "";
            		   
            		   String[] list4 = spectators.toArray(new String[spectators.size()]);
            		   int len4 = list4.length;
            		   String msg4 = "";
            		   
            		   for (int i = 0; i < len; i++){
            			   msg += list[i] +(i != len-1? ChatColor.WHITE +  ", " : "");
            			   }
            		   
            		   for (int i = 0; i < len2; i++){
            			   msg2 += list2[i] +(i != len2-1? ChatColor.WHITE +  ", " : "");
            			   }
            		   
            		   for (int i = 0; i < len3; i++){
            			   msg3 += list3[i] +(i != len3-1? ChatColor.WHITE+  ", " : "");
            			   }
            		   
            		   for (int i = 0; i < len4; i++){
            			   msg4 += list4[i] +(i != len4-1? ChatColor.WHITE +  ", " : "");
            			   }
            		   
            		   Boolean advmode = wi.isJoinIfOwnerOnline();
            		   String joinmode = wi.getAcess().toString().toLowerCase();
            		   sender.sendMessage(ChatColor.GOLD + "Список игроков и групп:");
            		   sender.sendMessage("Владельцы " + ChatColor.GOLD +"(" + len3 + ")" + ChatColor.WHITE + ": " + msg3);
            		   sender.sendMessage("Модераторы: " + ChatColor.GOLD +"(" + len2 + ")" + ChatColor.WHITE + ": " + msg2);
            		   sender.sendMessage("Участники: " + ChatColor.GOLD +"(" + len + ")" + ChatColor.WHITE + ": " + msg);
            		   sender.sendMessage("Наблюдатели: " + ChatColor.GOLD +"(" + len4 + ")" + ChatColor.WHITE + ": " + msg4);
            		   sender.sendMessage("Улучшенный режим: " + advmode.toString().replace("true", ChatColor.GREEN + "Активирован").replace("false", ChatColor.RED + "Деактивирован"));
            		   sender.sendMessage("Режим входа: " + joinmode.replace("access", ChatColor.RED + "Закрытый").replace("public", ChatColor.GREEN + "Публичный"));
    			   }
    			   else {
        			   sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
    			   }
	    	   }
	    	   else if (args[0].equalsIgnoreCase("access")) {
        		   String msg = "";
        		   sender.sendMessage(ChatColor.GOLD + "Список миров и уровень доступа:");
        		   for (WorldInfo wi : WorldLister.getContext().getAllWorlds()) {
            		   WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            		   String access = wi.getAcess().toString().toLowerCase().replace("access", "&cЗакрытый").replace("public", "&aПубличный");
            		   if (wp != null) {
              			   if (wp.getType() == PlayerType.SPECTATOR) {
            				   msg = "&fМир: '&6" + wi.getName() + "&f' = " + "&7Наблюдатель &f(" + access + "&f)";
            			   }
            			   else if (wp.getType() == PlayerType.MEMBER) {
            				   msg = "&fМир: '&6" + wi.getName() + "&f' = " + "&fУчастник &f(" + access + "&f)";
            			   }
            			   else if (wp.getType() == PlayerType.MODER) {
            				   msg = "&fМир: '&6" + wi.getName() + "&f' = " + "&eМодератор &f(" + access + "&f)";
            			   }
              			   else if (wp.getType() == PlayerType.OWNER) {
            				   msg = "&fМир: '&6" + wi.getName() + "&f' = " + "&6Владелец &f(" + access + "&f)";
            			   }
            		   }
            		   else {
            			   msg = "&fМир: '&6" + wi.getName() + "&f' = " + "&8Отсутствует запись &f(" + access + "&f)";
            		   }
        			   sender.sendMessage(msg.replace("&", "\u00a7"));
        		   }
	    	   }
	    	   else if (args[0].equalsIgnoreCase("players")) {
	    		   sender.sendMessage(ChatColor.GOLD + "Список игроков и в каких мирах они находятся:");
	    		   for (WorldInfo wi : WorldLister.getContext().getAllWorlds()) {
	    			   World w = Bukkit.getWorld(wi.getName());
					   List<String> players = new ArrayList<String>();
					   for (Player p : w.getPlayers()){
					       players.add(p.getName());
					   }
	        		   String[] lists = players.toArray(new String[players.size()]);
	        		   int lens = lists.length;
	        		   String msgs = "";
	        		   for (int i = 0; i < lens; i++){
	        			   msgs += lists[i] +(i != lens-1? ChatColor.GRAY + ", " : ChatColor.GRAY + "");
	        		   }
	        		   if (!w.getPlayers().isEmpty()) {
					        sender.sendMessage("Мир: '" + ChatColor.GOLD + wi.getName() + ChatColor.WHITE + "'. Игроки " + ChatColor.GOLD + "(" + lists.length + ")" + ChatColor.WHITE + ": " + ChatColor.GRAY + msgs);
	        		   }
	        		   else {
	       				   sender.sendMessage("Мир: '" + ChatColor.GOLD + wi.getName() + ChatColor.WHITE + "'. Игроки " + ChatColor.GOLD + "(" + lists.length + ")" + ChatColor.WHITE + ": ");
	        		   }
	        			   
	    		   }
	    		 
	    	   }
	    	   else if (args[0].equalsIgnoreCase("setspawn")) {
    			   WorldInfo wi = WorldLister.getContext().getWorldInfo(pl.getWorld().getName());
    			   if (wi != null) {
    				   WorldPlayer wp = wi.getWorldPlayer(pl.getName());
    				   if (wp != null) {
    					   if (wp.getType() == PlayerType.OWNER) {
            				   Location pll = pl.getLocation();
            				   Bukkit.getWorld(wi.getName()).setSpawnLocation(pll);
            				   int x = pll.getBlockX();
            				   int y = pll.getBlockY();
            				   int z = pll.getBlockZ();
            				   sender.sendMessage(MessageManager.get().getMessage("cmdSetSpawnSucc").replace("%X%","" + x).replace("%Y%","" + y).replace("%Z%","" + z).replace("%WORLD%", pl.getWorld().getName()).replace("&", "\u00a7"));
    					   }
    					   else {
    						   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
    					   }
    				   }
    				   else {
    					   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
    				   }
    			   }
    			   else {
    				   sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
    			   }
	    	   }
	       	   else if (args[0].equalsIgnoreCase("advmode")) {
       			   WorldInfo wi = WorldLister.getContext().getWorldInfo(pl.getWorld().getName());
       			   if (wi != null) {
       				   WorldPlayer wp = wi.getWorldPlayer(pl.getName());
       				   if (wp != null) {
       					   if (wp.getType() == PlayerType.OWNER) {
       						   if (wi.isJoinIfOwnerOnline() == true) {
       							   wi.setJoinIfOwnerOnline(false);
       							   new UpdateSettingsThread(wi.getName(), false, wi.getAcess().toString());
       							   sender.sendMessage(MessageManager.get().getMessage("cmdDeactiveAdvMode").replace("&", "\u00a7"));
       							   for (Player pla : Bukkit.getWorld(wi.getName()).getPlayers()) {
       								   pla.sendMessage(MessageManager.get().getMessage("cmdDeactiveAdvModeAll").replace("%PLAYER%", pl.getName()).replace("&", "\u00a7"));
       							   }
       						   }
       						   else {
       							   wi.setJoinIfOwnerOnline(true);
       							   new UpdateSettingsThread(wi.getName(), true, wi.getAcess().toString());
       							   sender.sendMessage(MessageManager.get().getMessage("cmdActiveAdvMode").replace("&", "\u00a7"));
       							   for (Player pla : Bukkit.getWorld(wi.getName()).getPlayers()) {
       								   pla.sendMessage(MessageManager.get().getMessage("cmdActiveAdvModeAll").replace("%PLAYER%", pl.getName()).replace("&", "\u00a7"));
       							   }
       						   }
       					   }
       					   else {
       						  sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
       					   }
       				   }
       				   else {
       					   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
       				   }
       			   }
       			   else {
       				   sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
       			   }
	       	   }
			}
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("addplayer")) {
                	String plw = pl.getWorld().getName();
                	WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
                	if (wi == null) {
                		sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
                		return true;
                	}
                	WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                	if (wp == null) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	PlayerType type = wp.getType();
                	if (!(type == PlayerType.MODER || type == PlayerType.OWNER)) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	Player pla = WorldLister.getPlayerRight(args[1]);
                	if (pla == null) {
                		sender.sendMessage(MessageManager.get().getMessage("playerNotExists").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
                		return true;
                	}
                	WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                	if (wpa != null) {
                		sender.sendMessage(MessageManager.get().getMessage("areWorldAdded").replace("&", "\u00a7"));
                		return true;
                	}
                	wi.addWorldPlayer(new WorldPlayer(args[1], PlayerType.SPECTATOR, System.currentTimeMillis(), pl.getName()));
    			    new PutWorldPlayerThread(plw, args[1], "SPECTATOR", System.currentTimeMillis(), pl.getName());
    			    sender.sendMessage(MessageManager.get().getMessage("addWorldSucc").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
    			    pla.sendMessage(MessageManager.get().getMessage("addWorldToPl").replace("%PLAYER%", args[1]).replace("%WORLD%", plw).replace("&", "\u00a7"));
	            }
	            else if (args[0].equalsIgnoreCase("removeplayer")) {
                	String plw = pl.getWorld().getName();
                	WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
                	if (wi == null) {
                		sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
                		return true;
                	}
                	WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                	if (wp == null) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	PlayerType type = wp.getType();
                	if (!(type == PlayerType.MODER || type == PlayerType.OWNER)) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                	if (wpa == null) {
    					sender.sendMessage(MessageManager.get().getMessage("remWorldNotF").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
    					return true; 
                	}
                	if (wpa.getName().equals(pl.getName())) {
        				sender.sendMessage(MessageManager.get().getMessage("yourSelfDeleteErr").replace("&", "\u00a7"));
        				return true; 
                	}
                	PlayerType delType = wpa.getType();
                	if (delType == PlayerType.MODER || delType == PlayerType.OWNER) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	wi.removeWorldPlayer(args[1]);
    			    new RemoveWorldPlayerThread(plw, args[1]);
    			    Player pla = WorldLister.getPlayerRight(args[1]);
    			    if (pla != null) {
    					if (pla.getLocation().getWorld().getName().equals(plw)) {
    						pla.teleport(Bukkit.getWorld("world").getSpawnLocation());
    						pla.sendMessage(MessageManager.get().getMessage("remWorldMsg1").replace("%WORLD%", plw).replace("%PLAYER%", pl.getName()).replace("&", "\u00a7"));
    					}
    					else {
    						pla.sendMessage(MessageManager.get().getMessage("remWorldMsg2").replace("%WORLD%", plw).replace("%PLAYER%", pl.getName()).replace("&", "\u00a7"));
    					}
    			    }
    			    sender.sendMessage(MessageManager.get().getMessage("remWorldSucc").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
	            }
	            else if (args[0].equalsIgnoreCase("spawn")) {
            		WorldInfo wi = WorldLister.getContext().getWorldInfo(args[1]);
            		if (wi == null) {
                		sender.sendMessage(MessageManager.get().getMessage("cmdSpawnWorldNotExist").replace("%WORLD%", args[1]).replace("&", "\u00a7"));
                		return true;  
            		}
            		if (wi.getAcess() == WorldAccess.PUBLIC) {
            			pl.teleport(Bukkit.getWorld(args[1]).getSpawnLocation());
            			sender.sendMessage(MessageManager.get().getMessage("cmdSpawnSucc").replace("&", "\u00a7"));
                		return true;  
            		}
            		WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            		if (wp == null) {
                		sender.sendMessage(MessageManager.get().getMessage("teleportPerms").replace("%WORLD%", args[1]).replace("&", "\u00a7"));
                		return true;  
            		}
            		PlayerType type = wp.getType();
            		if (type == PlayerType.OWNER || type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
            			pl.teleport(Bukkit.getWorld(args[1]).getSpawnLocation());
            			sender.sendMessage(MessageManager.get().getMessage("cmdSpawnSucc").replace("&", "\u00a7"));
            		}
            		else {
            			sender.sendMessage(MessageManager.get().getMessage("teleportPerms").replace("%WORLD%", args[1]).replace("&", "\u00a7"));
            		}
	            }
	     	   else if (args[0].equalsIgnoreCase("accessmode")) {
       			   WorldInfo wi = WorldLister.getContext().getWorldInfo(pl.getWorld().getName());
       			   if (wi != null) {
       				   WorldPlayer wp = wi.getWorldPlayer(pl.getName());
       				   if (wp != null) {
       					   if (wp.getType() == PlayerType.OWNER) {
       						   if (args[1].equalsIgnoreCase("public")) {
       							   if (wi.getAcess() == WorldAccess.PUBLIC) {
       								   sender.sendMessage(MessageManager.get().getMessage("cmdChangeModeIsActivated").replace("&", "\u00a7"));
       								   return true;
       							   }
       							   wi.setAccess(WorldAccess.PUBLIC);
       							   new UpdateSettingsThread(wi.getName(), wi.isJoinIfOwnerOnline(), "PUBLIC");
       							   sender.sendMessage(MessageManager.get().getMessage("cmdChangeModePublic").replace("&", "\u00a7"));
       							   for (Player pla : Bukkit.getOnlinePlayers()) {
       								   pla.sendMessage(MessageManager.get().getMessage("cmdChangeModePublicAll").replace("%PLAYER%", pl.getName()).replace("%WORLD%", pl.getWorld().getName()).replace("&", "\u00a7"));
       							   }
       						   }
       						   else if (args[1].equalsIgnoreCase("access")) {
       							   if (wi.getAcess() == WorldAccess.ACCESS) {
       								   sender.sendMessage(MessageManager.get().getMessage("cmdChangeModeIsActivated").replace("&", "\u00a7"));
       								   return true;
       							   }
       							   wi.setAccess(WorldAccess.ACCESS);
      							   new UpdateSettingsThread(wi.getName(), wi.isJoinIfOwnerOnline(), "ACCESS");
       							   sender.sendMessage(MessageManager.get().getMessage("cmdChangeModeAccess").replace("&", "\u00a7"));
       							   for (Player pla : Bukkit.getOnlinePlayers()) {
       								   pla.sendMessage(MessageManager.get().getMessage("cmdChangeModeAccessAll").replace("%PLAYER%", pl.getName()).replace("%WORLD%", pl.getWorld().getName()).replace("&", "\u00a7"));
       							   }
       						   }
       						   else {
       							sender.sendMessage("Аргумент должен быть значением 'public' или 'access'");
       						   }
       					   }
       					   else {
       						   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
       					   }
       				   }
       				   else {
       					   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
       				   }
       			   }
       			   else {
       				  sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
       			   }
	       	   }
	     	   else if (args[0].equalsIgnoreCase("player")) {
       			   WorldInfo wi = WorldLister.getContext().getWorldInfo(pl.getWorld().getName());
       			   if (wi != null) {
       				   WorldPlayer wp = wi.getWorldPlayer(pl.getName());
       				   if (wp != null) {
       					   if (wp.getType() == PlayerType.OWNER || wp.getType() == PlayerType.MODER) {
       						   WorldPlayer wpa = wi.getWorldPlayer(args[1]);
       						   if (wpa != null) {
       							   sender.sendMessage("Ник: " + wpa.getName());
       							   sender.sendMessage("Мир: " + wi.getName());
       							   sender.sendMessage("Тип: " + wpa.getType());
       							   sender.sendMessage("Кем добавлен: " + wpa.getWhoAdd());
       							   sender.sendMessage("Был добавлен: " + (DateUtil.formatedTime(System.currentTimeMillis() - wpa.getTimeAdded(), true)) + "назад.");
       						   }
       						   else {
       							  sender.sendMessage(MessageManager.get().getMessage("remWorldNotF").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
       						   }
       					   }
       					   else {
       						   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
       					   }
       				   }
       				   else {
       					   sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
       				   }
       			   }
       			   else {
       				  sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
       			   }
	       	   }
			}
			if (args.length == 3) {
	            if (args[0].equalsIgnoreCase("set")) {
                	String plw = pl.getWorld().getName();
                	WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
                	if (wi == null) {
                		sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr").replace("&", "\u00a7"));
                		return true; 	
                	}
                	WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                	if (wp == null) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	PlayerType type = wp.getType();
                	if (!(type == PlayerType.OWNER || type == PlayerType.MODER)) {
            			sender.sendMessage(MessageManager.get().getMessage("editPerms").replace("&", "\u00a7"));
                		return true;
                	}
                	WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                	if (wpa == null) {
                		sender.sendMessage(MessageManager.get().getMessage("remWorldNotF").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
        				return true; 
                	}
                	PlayerType typea = wpa.getType();
                	if (type == PlayerType.OWNER) {
                		if (typea == PlayerType.OWNER) {
                			pl.sendMessage(MessageManager.get().getMessage("cmdNotAllowChangeType").replace("&", "\u00a7"));
                			return true;
                		}
                		if (args[2].equalsIgnoreCase("moder")) {
                			if (wpa.getType() == PlayerType.MODER) {
                				pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
                				return true;
                			}
                			wpa.setType(PlayerType.MODER);
            			    new UpdateWorldPlayerThread(plw, wpa.getName(), PlayerType.MODER.toString(), wpa.getTimeAdded(), wpa.getWhoAdd());
                			pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", "модератор").replace("&", "\u00a7"));
                			Player pla = WorldLister.getPlayerRight(args[1]);
                			if (pla != null) {
                				pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", "модератор").replace("%WORLD%", plw).replace("&", "\u00a7"));
                			}
                		}
                		else if (args[2].equalsIgnoreCase("member")) {
                			if (wpa.getType() == PlayerType.MEMBER) {
                				pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
                				return true;
                			}
                			wpa.setType(PlayerType.MEMBER);
            			    new UpdateWorldPlayerThread(plw, wpa.getName(), PlayerType.MEMBER.toString(), wpa.getTimeAdded(), wpa.getWhoAdd());
                			pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", "участник").replace("&", "\u00a7"));
                			Player pla = WorldLister.getPlayerRight(args[1]);
                			if (pla != null) {
                				pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", "участник").replace("%WORLD%", plw).replace("&", "\u00a7"));
                			}
                		}
                		else if (args[2].equalsIgnoreCase("spectator")) {
                			if (wpa.getType() == PlayerType.SPECTATOR) {
                				pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
                				return true;
                			}
                			wpa.setType(PlayerType.SPECTATOR);
            			    new UpdateWorldPlayerThread(plw, wpa.getName(), PlayerType.SPECTATOR.toString(), wpa.getTimeAdded(), wpa.getWhoAdd());
                			pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", "наблюдатель").replace("&", "\u00a7"));
                			Player pla = WorldLister.getPlayerRight(args[1]);
                			if (pla != null) {
                				pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", "наблюдатель").replace("%WORLD%", plw).replace("&", "\u00a7"));
                			}
                		}
                		else {
                			pl.sendMessage("Значение может быть только 'moder', 'member' и 'spectator'");
                		}
                	}
                	else if (type == PlayerType.MODER) {
                		if (typea == PlayerType.MODER || typea == PlayerType.OWNER) {
                			pl.sendMessage(MessageManager.get().getMessage("cmdNotAllowChangeType").replace("&", "\u00a7"));
                			return true;
                		}
                		if (args[2].equalsIgnoreCase("member")) {
                			if (wpa.getType() == PlayerType.MEMBER) {
                				pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
                				return true;
                			}
                			wpa.setType(PlayerType.MEMBER);
            			    new UpdateWorldPlayerThread(plw, wpa.getName(), PlayerType.MEMBER.toString(), wpa.getTimeAdded(), wpa.getWhoAdd());
                			pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", "участник").replace("&", "\u00a7"));
                			Player pla = WorldLister.getPlayerRight(args[1]);
                			if (pla != null) {
                				pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", "участник").replace("%WORLD%", plw).replace("&", "\u00a7"));
                			}
                		}
                		else if (args[2].equalsIgnoreCase("spectator")) {
                			if (wpa.getType() == PlayerType.SPECTATOR) {
                				pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]).replace("&", "\u00a7"));
                				return true;
                			}
                			wpa.setType(PlayerType.SPECTATOR);
            			    new UpdateWorldPlayerThread(plw, wpa.getName(), PlayerType.SPECTATOR.toString(), wpa.getTimeAdded(), wpa.getWhoAdd());
                			pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", "наблюдатель").replace("&", "\u00a7"));
                			Player pla = WorldLister.getPlayerRight(args[1]);
                			if (pla != null) {
                				pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", "наблюдатель").replace("%WORLD%", plw).replace("&", "\u00a7"));
                			}
                		}
                		else {
                			pl.sendMessage("Значение может быть только 'member' или 'spectator'");
                		}
                	}
	            }
			}
		}
		else if (sender instanceof ConsoleCommandSender) {
			if (args.length == 0) {
				sender.sendMessage("/wol addowner <мир> <ник>");
				sender.sendMessage("/wol removeowner <мир> <ник>");
				sender.sendMessage("/wol reload");
			}
			if (args.length == 1) {
		    	   if (args[0].equalsIgnoreCase("reload")) {
	    			   WorldLister.getContext().reloadConfig();
	    			   MessageManager.get().loadMessages();
	    			   sender.sendMessage(ChatColor.GOLD + "Конфигурация сообщений перезагружена.");
		    	   }
			}
			if (args.length == 3) {
	            if (args[0].equalsIgnoreCase("addowner")) {
            		WorldInfo wi = WorldLister.getContext().getWorldInfo(args[1]);
            		if (wi != null) {
            			WorldPlayer wp = wi.getWorldPlayer(args[2]);
            			if (wp != null) {
            				if (wp.getType() == PlayerType.OWNER) {
            					sender.sendMessage("Игрок уже является владельцем!");
            					return true;
            				}
            				wp.setType(PlayerType.OWNER);
            			    new UpdateWorldPlayerThread(wi.getName(), wp.getName(), PlayerType.OWNER.toString(), wp.getTimeAdded(), wp.getWhoAdd());
            			}
            			else {
            				wi.addWorldPlayer(new WorldPlayer(args[2], PlayerType.OWNER, System.currentTimeMillis(), sender.getName()));
            			    new PutWorldPlayerThread(args[1], args[2], PlayerType.OWNER.toString(), System.currentTimeMillis(), sender.getName());
            			}
            			sender.sendMessage("Игрок успешно установлен владельцем мира " + args[1]);
            		}
            		else {
            			sender.sendMessage("Мир не найден");
            		}
	            }
	            else if (args[0].equalsIgnoreCase("removeowner")) {
            		WorldInfo wi = WorldLister.getContext().getWorldInfo(args[1]);
            		if (wi != null) {
            			WorldPlayer wp = wi.getWorldPlayer(args[2]);
            			if (wp != null) {
            				if (wp.getType() == PlayerType.OWNER) {
            					wi.removeWorldPlayer(args[2]);
                			    new RemoveWorldPlayerThread(args[1], args[2]);
                			    sender.sendMessage("Игрок успешно удален из владельцев мира " + args[1]);
            				}
            				else {
            					sender.sendMessage("Игрок не владелец.");
            				}
            			}
            			else {
            				sender.sendMessage("Игрок не найден в это мире.");
            			}
            		}
            		else {
            			sender.sendMessage("Мир не найден");
            		}
	            }
			}
		}
		else {
			sender.sendMessage("Отправляющий команду должен быть игроком или консолью.");
		}
		return true;
	}

	private static final List<String> empty = new ArrayList<>();
	
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if (arg0 instanceof Player) {
			Player pl = (Player) arg0;
			if (arg3.length == 1) {
				List<String> ss = new ArrayList<String>(Arrays.asList("spawn", "set", "addplayer", "player", "removeplayer", "access", "list", "players", "setspawn", "advmode", "accessmode"));
		        List<String> matches = new ArrayList<>();
	            String search = arg3[0].toLowerCase();
	            for (String s : ss)
	            {
	                if (s.toLowerCase().startsWith(search))
	                {
	                    matches.add(s);
	                }
	            }
	        return matches;
			}
			if (arg3.length == 2) {
				if (arg3[0].equalsIgnoreCase("spawn")) {
					List<String> acceptedWorlds = new ArrayList<>();
					for (WorldInfo wi : WorldLister.getContext().getAllWorlds()) {
						if (wi.getAcess() == WorldAccess.PUBLIC) {
							acceptedWorlds.add(wi.getName());
							continue;
						}
						WorldPlayer wp = wi.getWorldPlayer(pl.getName());
						if (wp == null) {
							continue;
						}
						if (wp.getType() == PlayerType.MEMBER || wp.getType() == PlayerType.OWNER || wp.getType() == PlayerType.MODER || wp.getType() == PlayerType.SPECTATOR) {
							acceptedWorlds.add(wi.getName());
						}
					}
			        List<String> matches = new ArrayList<>();
		            String search = arg3[1].toLowerCase();
		            for (String world : acceptedWorlds)
		            {
		                if (world.toLowerCase().startsWith(search))
		                {
		                    matches.add(world);
		                }
		            }
		        return matches;
					
				}
				else if (arg3[0].equalsIgnoreCase("addplayer")) {
					String plw = pl.getWorld().getName();
					WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
					if (wi != null) {
						WorldPlayer wp = wi.getWorldPlayer(pl.getName());
						if (wp == null) {
							return empty;
						}
						if (!(wp.getType() == PlayerType.OWNER || wp.getType() == PlayerType.MODER)) {
							return empty;
						}
						List<String> notAdded = new ArrayList<String>();
						for (Player pla : Bukkit.getOnlinePlayers()) {
							if (!wi.getAllWorldPlayersString().contains(pla.getName())) {
								notAdded.add(pla.getName());
							}
						}
 				        List<String> matches = new ArrayList<>();
 			            String search = arg3[1].toLowerCase();
 			            for (String player : notAdded)
 			            {
 			                if (player.toLowerCase().startsWith(search))
 			                {
 			                    matches.add(player);
 			                }
 			            }	
 				        return matches;
					}
					else {
						return empty;
					}
				}
				else if (arg3[0].equalsIgnoreCase("removeplayer") || arg3[0].equalsIgnoreCase("set") || arg3[0].equalsIgnoreCase("player")) {
					String plw = pl.getWorld().getName();
					WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
					if (wi != null) {
						WorldPlayer wp = wi.getWorldPlayer(pl.getName());
						if (wp == null) {
							return empty;
						}
						if (!(wp.getType() == PlayerType.OWNER || wp.getType() == PlayerType.MODER)) {
							return empty;
						}
						List<String> ss = new ArrayList<String>(wi.getAllWorldPlayersString());
				        List<String> matches = new ArrayList<>();
			            String search = arg3[1].toLowerCase();
			            for (String player : ss)
			            {
			                if (player.toLowerCase().startsWith(search))
			                {
			                    matches.add(player);
			                }
			            }	
				        return matches;
					}
					else {
						return empty;
					}
				}
				else if (arg3[0].equalsIgnoreCase("accessmode")) {
					String plw = pl.getWorld().getName();
					WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
					if (wi != null) {
						WorldPlayer wp = wi.getWorldPlayer(pl.getName());
						if (wp == null) {
							return empty;
						}
						if (wp.getType() != PlayerType.OWNER) {
							return empty;
						}
						List<String> ss = new ArrayList<String>(Arrays.asList("public", "access"));
				        List<String> matches = new ArrayList<>();
			            String search = arg3[1].toLowerCase();
			            for (String player : ss)
			            {
			                if (player.toLowerCase().startsWith(search))
			                {
			                    matches.add(player);
			                }
			            }	
				        return matches;
					}
					else {
						return empty;
					}
				}
				else {
					return empty;
				}
			}
			if (arg3.length == 3) {
				if (arg3[0].equalsIgnoreCase("set")) {
					String plw = pl.getWorld().getName();
					WorldInfo wi = WorldLister.getContext().getWorldInfo(plw);
					if (wi != null) {
						WorldPlayer wp = wi.getWorldPlayer(pl.getName());
						if (wp == null) {
							return empty;
						}
						List<String> ss;
						if (wp.getType() == PlayerType.OWNER) {
							ss = new ArrayList<String>(Arrays.asList("spectator", "member", "moder"));
						}
						else if (wp.getType() == PlayerType.MODER) {
							ss = new ArrayList<String>(Arrays.asList("spectator", "member"));
						}
						else {
							return empty;
						}
				        List<String> matches = new ArrayList<>();
			            String search = arg3[2].toLowerCase();
			            for (String player : ss)
			            {
			                if (player.toLowerCase().startsWith(search))
			                {
			                    matches.add(player);
			                }
			            }	
				        return matches;
					}
					else {
						return empty;
					}
				}
				else {
					return empty;
				}
			}
			else {
				return empty;
			}
		}
		else {
			if (arg3.length == 1) {
				List<String> ss = new ArrayList<String>(Arrays.asList("addowner", "removeowner", "players"));
		        List<String> matches = new ArrayList<>();
	            String search = arg3[0].toLowerCase();
	            for (String s : ss)
	            {
	                if (s.toLowerCase().startsWith(search))
	                {
	                    matches.add(s);
	                }
	            }
	        return matches;
			}
			if (arg3.length == 2) {
				if (arg3[0].equalsIgnoreCase("addowner") || arg3[0].equalsIgnoreCase("removeowner")) {
					List<String> worlds = new ArrayList<String>();
					for (World w : Bukkit.getWorlds()) {
						worlds.add(w.getName());
					}
			        List<String> matches = new ArrayList<>();
		            String search = arg3[1].toLowerCase();
		            for (String player : worlds)
		            {
		                if (player.toLowerCase().startsWith(search))
		                {
		                    matches.add(player);
		                }
		            }	
			        return matches;
				}
			}
			if (arg3.length == 3) {
				if (arg3[0].equalsIgnoreCase("addowner")) {
					List<String> players = new ArrayList<String>();
					for (Player pl : Bukkit.getOnlinePlayers()) {
						players.add(pl.getName());
					}
			        List<String> matches = new ArrayList<>();
		            String search = arg3[2].toLowerCase();
		            for (String player : players)
		            {
		                if (player.toLowerCase().startsWith(search))
		                {
		                    matches.add(player);
		                }
		            }	
			        return matches;
				}
				else if (arg3[0].equalsIgnoreCase("removeowner")) {
					WorldInfo wi = WorldLister.getContext().getWorldInfo(arg3[1]);
					if (wi == null) {
						return empty;
					}
					List<String> players = new ArrayList<String>(wi.getAllWorldPlayersString());
			        List<String> matches = new ArrayList<>();
		            String search = arg3[2].toLowerCase();
		            for (String player : players)
		            {
		                if (player.toLowerCase().startsWith(search))
		                {
		                    matches.add(player);
		                }
		            }	
			        return matches;
				}
				else {
					return empty;
				}
			}
			else {
				return empty;
			}
		}
	}
	
}
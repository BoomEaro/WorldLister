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
import ru.boomearo.worldlister.database.Sql;
import ru.boomearo.worldlister.managers.MessageManager;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.WorldAccessType;
import ru.boomearo.worldlister.objects.ProtectedWorld;
import ru.boomearo.worldlister.objects.WorldPlayer;
import ru.boomearo.worldlister.utils.DateUtil;

public class Commands implements CommandExecutor, TabCompleter {

    private static final List<String> empty = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (!(pl.hasPermission("worldlister.commands"))) {
                pl.sendMessage(MessageManager.get().getMessage("dontHavePermissions"));
                return true;
            }
            if (args.length == 0) {
                for (String s : MessageManager.get().getMessageList("help")) {
                    sender.sendMessage(s.replace("{version}", WorldLister.getInstance().getDescription().getVersion()));
                }
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(pl.getWorld().getName());
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

                        for (int i = 0; i < len; i++) {
                            msg += list[i] + (i != len - 1 ? ChatColor.WHITE + ", " : "");
                        }

                        for (int i = 0; i < len2; i++) {
                            msg2 += list2[i] + (i != len2 - 1 ? ChatColor.WHITE + ", " : "");
                        }

                        for (int i = 0; i < len3; i++) {
                            msg3 += list3[i] + (i != len3 - 1 ? ChatColor.WHITE + ", " : "");
                        }

                        for (int i = 0; i < len4; i++) {
                            msg4 += list4[i] + (i != len4 - 1 ? ChatColor.WHITE + ", " : "");
                        }

                        sender.sendMessage(ChatColor.GOLD + "Список игроков и групп:");
                        sender.sendMessage("Владельцы " + ChatColor.GOLD + "(" + len3 + ")" + ChatColor.WHITE + ": " + msg3);
                        sender.sendMessage("Модераторы: " + ChatColor.GOLD + "(" + len2 + ")" + ChatColor.WHITE + ": " + msg2);
                        sender.sendMessage("Участники: " + ChatColor.GOLD + "(" + len + ")" + ChatColor.WHITE + ": " + msg);
                        sender.sendMessage("Наблюдатели: " + ChatColor.GOLD + "(" + len4 + ")" + ChatColor.WHITE + ": " + msg4);
                        sender.sendMessage("Улучшенный режим: " + (wi.isJoinIfOwnerOnline() ? ChatColor.GREEN + "Активирован" : ChatColor.RED + "Деактивирован"));
                        sender.sendMessage("Режим входа: " + wi.getAccess().getName());
                    }
                    else {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                    }
                }
                else if (args[0].equalsIgnoreCase("access")) {
                    String msg = "";
                    sender.sendMessage(ChatColor.GOLD + "Список миров и уровень доступа:");
                    for (ProtectedWorld wi : WorldLister.getInstance().getProtectedWorldManager().getAllProtectedWorlds()) {
                        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                        String access = wi.getAccess().getName();
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
                        sender.sendMessage(msg);
                    }
                }
                else if (args[0].equalsIgnoreCase("players")) {
                    sender.sendMessage(ChatColor.GOLD + "Список игроков и в каких мирах они находятся:");
                    for (ProtectedWorld wi : WorldLister.getInstance().getProtectedWorldManager().getAllProtectedWorlds()) {
                        World w = wi.getWorld();
                        List<String> players = new ArrayList<String>();
                        for (Player p : w.getPlayers()) {
                            players.add(p.getName());
                        }
                        String[] lists = players.toArray(new String[players.size()]);
                        int lens = lists.length;
                        String msgs = "";
                        for (int i = 0; i < lens; i++) {
                            msgs += lists[i] + (i != lens - 1 ? ChatColor.GRAY + ", " : ChatColor.GRAY + "");
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
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(pl.getWorld().getName());
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    if (wp.getType() != PlayerType.OWNER) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }

                    Location pll = pl.getLocation();
                    wi.getWorld().setSpawnLocation(pll);

                    int x = pll.getBlockX();
                    int y = pll.getBlockY();
                    int z = pll.getBlockZ();

                    sender.sendMessage(MessageManager.get().getMessage("cmdSetSpawnSucc").replace("%X%", "" + x).replace("%Y%", "" + y).replace("%Z%", "" + z).replace("%WORLD%", pl.getWorld().getName()));
                }
                else if (args[0].equalsIgnoreCase("advmode")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(pl.getWorld().getName());
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    if (wp.getType() != PlayerType.OWNER) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }

                    wi.setJoinIfOwnerOnline(!wi.isJoinIfOwnerOnline());

                    Sql.getInstance().updateSettings(wi.getName(), wi.isJoinIfOwnerOnline(), wi.getAccess());

                    if (wi.isJoinIfOwnerOnline()) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdActiveAdvMode"));
                        for (Player pla : wi.getWorld().getPlayers()) {
                            pla.sendMessage(MessageManager.get().getMessage("cmdActiveAdvModeAll").replace("%PLAYER%", pl.getName()));
                        }
                    }
                    else {
                        sender.sendMessage(MessageManager.get().getMessage("cmdDeactiveAdvMode"));
                        for (Player pla : wi.getWorld().getPlayers()) {
                            pla.sendMessage(MessageManager.get().getMessage("cmdDeactiveAdvModeAll").replace("%PLAYER%", pl.getName()));
                        }
                    }
                }
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("addplayer")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    PlayerType type = wp.getType();
                    if (!(type == PlayerType.MODER || type == PlayerType.OWNER)) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    Player pla = WorldLister.getPlayerRight(args[1]);
                    if (pla == null) {
                        sender.sendMessage(MessageManager.get().getMessage("playerNotExists").replace("%PLAYER%", args[1]));
                        return true;
                    }
                    WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                    if (wpa != null) {
                        sender.sendMessage(MessageManager.get().getMessage("areWorldAdded"));
                        return true;
                    }

                    wi.addWorldPlayer(new WorldPlayer(args[1], PlayerType.SPECTATOR, System.currentTimeMillis(), pl.getName()));
                    Sql.getInstance().putWorldPlayer(plw, args[1], PlayerType.SPECTATOR, System.currentTimeMillis(), pl.getName());

                    sender.sendMessage(MessageManager.get().getMessage("addWorldSucc").replace("%PLAYER%", args[1]));
                    pla.sendMessage(MessageManager.get().getMessage("addWorldToPl").replace("%PLAYER%", args[1]).replace("%WORLD%", plw));
                }
                else if (args[0].equalsIgnoreCase("removeplayer")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    PlayerType type = wp.getType();
                    if (!(type == PlayerType.MODER || type == PlayerType.OWNER)) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                    if (wpa == null) {
                        sender.sendMessage(MessageManager.get().getMessage("remWorldNotF").replace("%PLAYER%", args[1]));
                        return true;
                    }
                    if (wpa.getName().equals(pl.getName())) {
                        sender.sendMessage(MessageManager.get().getMessage("yourSelfDeleteErr"));
                        return true;
                    }
                    PlayerType delType = wpa.getType();
                    if (delType == PlayerType.MODER || delType == PlayerType.OWNER) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }

                    wi.removeWorldPlayer(args[1]);
                    Sql.getInstance().removeWorldPlayer(plw, args[1]);

                    Player pla = WorldLister.getPlayerRight(args[1]);
                    if (pla != null) {
                        if (pla.getLocation().getWorld().getName().equals(plw)) {
                            pla.teleport(WorldLister.getMainWorld().getSpawnLocation());
                            pla.sendMessage(MessageManager.get().getMessage("remWorldMsg1").replace("%WORLD%", plw).replace("%PLAYER%", pl.getName()));
                        }
                        else {
                            pla.sendMessage(MessageManager.get().getMessage("remWorldMsg2").replace("%WORLD%", plw).replace("%PLAYER%", pl.getName()));
                        }
                    }
                    sender.sendMessage(MessageManager.get().getMessage("remWorldSucc").replace("%PLAYER%", args[1]));
                }
                else if (args[0].equalsIgnoreCase("spawn")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(args[1]);
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdSpawnWorldNotExist").replace("%WORLD%", args[1]));
                        return true;
                    }
                    if (wi.getAccess() == WorldAccessType.PUBLIC) {
                        pl.teleport(wi.getWorld().getSpawnLocation());
                        sender.sendMessage(MessageManager.get().getMessage("cmdSpawnSucc"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("teleportPerms").replace("%WORLD%", args[1]));
                        return true;
                    }
                    PlayerType type = wp.getType();
                    if (type == PlayerType.OWNER || type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        pl.teleport(wi.getWorld().getSpawnLocation());
                        sender.sendMessage(MessageManager.get().getMessage("cmdSpawnSucc"));
                    }
                    else {
                        sender.sendMessage(MessageManager.get().getMessage("teleportPerms").replace("%WORLD%", args[1]));
                    }
                }
                else if (args[0].equalsIgnoreCase("accessmode")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(pl.getWorld().getName());
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    if (wp.getType() != PlayerType.OWNER) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }

                    WorldAccessType type = null;
                    try {
                        type = WorldAccessType.valueOf(args[1].toUpperCase());
                    }
                    catch (Exception e) {
                    }
                    if (type == null) {
                        sender.sendMessage("Аргумент должен быть значением 'public' или 'access'");
                        return true;
                    }

                    if (wi.getAccess() == type) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdChangeModeIsActivated"));
                        return true;
                    }

                    wi.setAccess(type);
                    Sql.getInstance().updateSettings(wi.getName(), wi.isJoinIfOwnerOnline(), type);

                    if (type == WorldAccessType.PUBLIC) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdChangeModePublic"));
                        for (Player pla : Bukkit.getOnlinePlayers()) {
                            pla.sendMessage(MessageManager.get().getMessage("cmdChangeModePublicAll").replace("%PLAYER%", pl.getName()).replace("%WORLD%", pl.getWorld().getName()));
                        }
                    }
                    else if (type == WorldAccessType.ACCESS) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdChangeModeAccess"));
                        for (Player pla : Bukkit.getOnlinePlayers()) {
                            pla.sendMessage(MessageManager.get().getMessage("cmdChangeModeAccessAll").replace("%PLAYER%", pl.getName()).replace("%WORLD%", pl.getWorld().getName()));
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("player")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(pl.getWorld().getName());
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    if (!(wp.getType() == PlayerType.OWNER || wp.getType() == PlayerType.MODER)) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                    if (wpa == null) {
                        sender.sendMessage(MessageManager.get().getMessage("remWorldNotF").replace("%PLAYER%", args[1]));
                        return true;
                    }

                    sender.sendMessage("Ник: " + wpa.getName());
                    sender.sendMessage("Мир: " + wi.getName());
                    sender.sendMessage("Тип: " + wpa.getType().getName());
                    sender.sendMessage("Кем добавлен: " + wpa.getWhoAdd());
                    sender.sendMessage("Был добавлен: " + (DateUtil.formatedTime(System.currentTimeMillis() - wpa.getTimeAdded(), true)) + " назад.");
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("set")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
                    if (wi == null) {
                        sender.sendMessage(MessageManager.get().getMessage("cmdNotWorldErr"));
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                    if (wp == null) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    PlayerType editorType = wp.getType();
                    if (!(editorType == PlayerType.OWNER || editorType == PlayerType.MODER)) {
                        sender.sendMessage(MessageManager.get().getMessage("editPerms"));
                        return true;
                    }
                    WorldPlayer wpa = wi.getWorldPlayer(args[1]);
                    if (wpa == null) {
                        sender.sendMessage(MessageManager.get().getMessage("remWorldNotF").replace("%PLAYER%", args[1]));
                        return true;
                    }

                    PlayerType targetType = wpa.getType();
                    PlayerType newType = null;
                    try {
                        newType = PlayerType.valueOf(args[2].toUpperCase());
                    }
                    catch (Exception e) {
                    }
                    if (newType == null) {
                        pl.sendMessage("Значение может быть только 'moder', 'member' и 'spectator'");
                        return true;
                    }

                    if (editorType == PlayerType.OWNER) {
                        if (targetType == PlayerType.OWNER) {
                            pl.sendMessage(MessageManager.get().getMessage("cmdNotAllowChangeType"));
                            return true;
                        }

                        if (wpa.getType() == newType) {
                            pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]));
                            return true;
                        }

                        wpa.setType(newType);
                        Sql.getInstance().updateWorldPlayer(plw, wpa.getName(), newType, wpa.getTimeAdded(), wpa.getWhoAdd());

                        pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", newType.getName()));
                        Player pla = WorldLister.getPlayerRight(args[1]);
                        if (pla != null) {
                            pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", newType.getName()).replace("%WORLD%", plw));
                        }
                    }
                    else if (editorType == PlayerType.MODER) {
                        if (targetType == PlayerType.MODER || targetType == PlayerType.OWNER) {
                            pl.sendMessage(MessageManager.get().getMessage("cmdNotAllowChangeType"));
                            return true;
                        }

                        if (wpa.getType() == newType) {
                            pl.sendMessage(MessageManager.get().getMessage("setWorldAre").replace("%PLAYER%", args[1]));
                            return true;
                        }

                        if (!(newType == PlayerType.MEMBER || newType == PlayerType.SPECTATOR)) {
                            pl.sendMessage("Значение может быть только 'member' или 'spectator'");
                            return true;
                        }

                        wpa.setType(newType);
                        Sql.getInstance().updateWorldPlayer(plw, wpa.getName(), newType, wpa.getTimeAdded(), wpa.getWhoAdd());

                        pl.sendMessage(MessageManager.get().getMessage("setWorldPl").replace("%PLAYER%", args[1]).replace("%TYPE%", newType.getName()));
                        Player pla = WorldLister.getPlayerRight(args[1]);
                        if (pla != null) {
                            pla.sendMessage(MessageManager.get().getMessage("setWorldToPl").replace("%PLAYER%", pl.getName()).replace("%TYPE%", newType.getName()).replace("%WORLD%", plw));
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
                    WorldLister.getInstance().reloadConfig();
                    MessageManager.get().loadMessages();
                    sender.sendMessage(ChatColor.GOLD + "Конфигурация сообщений перезагружена.");
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("addowner")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(args[1]);
                    if (wi == null) {
                        sender.sendMessage("Мир не найден");
                        return true;
                    }

                    WorldPlayer wp = wi.getWorldPlayer(args[2]);
                    if (wp != null) {
                        if (wp.getType() == PlayerType.OWNER) {
                            sender.sendMessage("Игрок уже является владельцем!");
                            return true;
                        }
                        wp.setType(PlayerType.OWNER);
                        Sql.getInstance().updateWorldPlayer(wi.getName(), wp.getName(), PlayerType.OWNER, wp.getTimeAdded(), wp.getWhoAdd());
                    }
                    else {
                        wi.addWorldPlayer(new WorldPlayer(args[2], PlayerType.OWNER, System.currentTimeMillis(), sender.getName()));
                        Sql.getInstance().updateWorldPlayer(args[1], args[2], PlayerType.OWNER, System.currentTimeMillis(), sender.getName());
                    }
                    sender.sendMessage("Игрок успешно установлен владельцем мира " + args[1]);
                }
                else if (args[0].equalsIgnoreCase("removeowner")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(args[1]);
                    if (wi == null) {
                        sender.sendMessage("Мир не найден");
                        return true;
                    }
                    WorldPlayer wp = wi.getWorldPlayer(args[2]);
                    if (wp == null) {
                        sender.sendMessage("Игрок не найден в это мире.");
                        return true;
                    }
                    if (wp.getType() != PlayerType.OWNER) {
                        sender.sendMessage("Игрок не владелец.");
                        return true;
                    }

                    wi.removeWorldPlayer(args[2]);
                    Sql.getInstance().removeWorldPlayer(args[1], args[2]);

                    sender.sendMessage("Игрок успешно удален из владельцев мира " + args[1]);
                }
            }
        }
        else {
            sender.sendMessage("Отправляющий команду должен быть игроком или консолью.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (args.length == 1) {
                List<String> matches = new ArrayList<>();
                String search = args[0].toLowerCase();
                for (String s : Arrays.asList("spawn", "set", "addplayer", "player", "removeplayer", "access", "list", "players", "setspawn", "advmode", "accessmode")) {
                    if (s.toLowerCase().startsWith(search)) {
                        matches.add(s);
                    }
                }
                return matches;
            }
            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("spawn")) {
                    List<String> acceptedWorlds = new ArrayList<>();
                    for (ProtectedWorld wi : WorldLister.getInstance().getProtectedWorldManager().getAllProtectedWorlds()) {
                        if (wi.getAccess() == WorldAccessType.PUBLIC) {
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
                    String search = args[1].toLowerCase();
                    for (String world : acceptedWorlds) {
                        if (world.toLowerCase().startsWith(search)) {
                            matches.add(world);
                        }
                    }
                    return matches;

                }
                else if (args[0].equalsIgnoreCase("addplayer")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
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
                        String search = args[1].toLowerCase();
                        for (String player : notAdded) {
                            if (player.toLowerCase().startsWith(search)) {
                                matches.add(player);
                            }
                        }
                        return matches;
                    }
                }
                else if (args[0].equalsIgnoreCase("removeplayer") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("player")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
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
                        String search = args[1].toLowerCase();
                        for (String player : ss) {
                            if (player.toLowerCase().startsWith(search)) {
                                matches.add(player);
                            }
                        }
                        return matches;
                    }
                }
                else if (args[0].equalsIgnoreCase("accessmode")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
                    if (wi != null) {
                        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
                        if (wp == null) {
                            return empty;
                        }
                        if (wp.getType() != PlayerType.OWNER) {
                            return empty;
                        }
                        List<String> matches = new ArrayList<>();
                        String search = args[1].toLowerCase();
                        for (WorldAccessType type : WorldAccessType.values()) {
                            if (type.name().toLowerCase().startsWith(search)) {
                                matches.add(type.name());
                            }
                        }
                        return matches;
                    }
                }
            }
            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("set")) {
                    String plw = pl.getWorld().getName();
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(plw);
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
                        String search = args[2].toLowerCase();
                        for (String player : ss) {
                            if (player.toLowerCase().startsWith(search)) {
                                matches.add(player);
                            }
                        }
                        return matches;
                    }
                }
            }
        }
        else {
            if (args.length == 1) {
                List<String> ss = new ArrayList<String>(Arrays.asList("addowner", "removeowner", "players"));
                List<String> matches = new ArrayList<>();
                String search = args[0].toLowerCase();
                for (String s : ss) {
                    if (s.toLowerCase().startsWith(search)) {
                        matches.add(s);
                    }
                }
                return matches;
            }
            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("addowner") || args[0].equalsIgnoreCase("removeowner")) {
                    List<String> worlds = new ArrayList<String>();
                    for (World w : Bukkit.getWorlds()) {
                        worlds.add(w.getName());
                    }
                    List<String> matches = new ArrayList<>();
                    String search = args[1].toLowerCase();
                    for (String player : worlds) {
                        if (player.toLowerCase().startsWith(search)) {
                            matches.add(player);
                        }
                    }
                    return matches;
                }
            }
            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("addowner")) {
                    List<String> players = new ArrayList<String>();
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        players.add(pl.getName());
                    }
                    List<String> matches = new ArrayList<>();
                    String search = args[2].toLowerCase();
                    for (String player : players) {
                        if (player.toLowerCase().startsWith(search)) {
                            matches.add(player);
                        }
                    }
                    return matches;
                }
                else if (args[0].equalsIgnoreCase("removeowner")) {
                    ProtectedWorld wi = WorldLister.getInstance().getProtectedWorldManager().getProtectedWorld(args[1]);
                    if (wi == null) {
                        return empty;
                    }
                    List<String> players = new ArrayList<String>(wi.getAllWorldPlayersString());
                    List<String> matches = new ArrayList<>();
                    String search = args[2].toLowerCase();
                    for (String player : players) {
                        if (player.toLowerCase().startsWith(search)) {
                            matches.add(player);
                        }
                    }
                    return matches;
                }
            }
        }
        return empty;
    }

}

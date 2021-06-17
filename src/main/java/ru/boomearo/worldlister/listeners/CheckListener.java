package ru.boomearo.worldlister.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import ru.boomearo.worldlister.WorldLister;
import ru.boomearo.worldlister.managers.MessageManager;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.WorldAccess;
import ru.boomearo.worldlister.objects.WorldInfo;
import ru.boomearo.worldlister.objects.WorldPlayer;

public class CheckListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
        String w = e.getTo().getWorld().getName();
        Player pl = e.getPlayer();
        if (pl.hasMetadata("NPC")) {
            return;
        }
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (e.getFrom().getWorld().equals(e.getTo().getWorld())) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("teleportPerms");
        if (wp == null) {
            pl.sendMessage(msg.replace("%WORLD%", w).replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (!(type == PlayerType.MODER || type == PlayerType.OWNER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR)) {
                pl.sendMessage(msg.replace("%WORLD%", w).replace("&", "\u00a7"));
                e.setCancelled(true);
            }
            if (type == PlayerType.SPECTATOR) {
                WorldLister.tpWorldClear(pl);
            }
        }
        else {
            if (!(type == PlayerType.MODER || type == PlayerType.OWNER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR)) {
                pl.sendMessage(msg.replace("%WORLD%", w).replace("&", "\u00a7"));
                e.setCancelled(true);
            }
            if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                WorldLister.tpWorldClear(pl);
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("joinPerms");
        if (wp == null) {
            pl.teleport(Bukkit.getWorld("world").getSpawnLocation());
            pl.sendMessage(msg.replace("%WORLD%", w).replace("&", "\u00a7"));
            return;
        }
        PlayerType type = wp.getType();
        if (!(type == PlayerType.MODER || type == PlayerType.OWNER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR)) {
            pl.teleport(Bukkit.getWorld("world").getSpawnLocation());
            pl.sendMessage(msg.replace("%WORLD%", w).replace("&", "\u00a7"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        Player pl = e.getPlayer();
        for (WorldInfo wi : WorldLister.getInstance().getAllWorlds()) {
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp != null) {
                PlayerType type = wp.getType();
                if (type == PlayerType.OWNER) {
                    if (wi.isJoinIfOwnerOnline()) {
                        List<String> playerOnline = new ArrayList<String>();
                        for (WorldPlayer wpa : wi.getAllWorldPlayers()) {
                            if (wpa.getType() == PlayerType.OWNER) {
                                if (WorldLister.getPlayerRight(wpa.getName()) != null) {
                                    playerOnline.add(wpa.getName());
                                }
                            }
                        }
                        if (playerOnline.size() <= 1) {
                            for (Player ppp : Bukkit.getWorld(wi.getName()).getPlayers()) {
                                String info1 = "&cПоследний владелец " + pl.getName() + " мира '" + wi.getName() + "' покинул сервер. Теперь у вас отсутствует доступ к модификции этого мира.";
                                ppp.sendMessage(info1.replace("&", "\u00a7"));
                            }
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specBreakFailed");
        if (wp == null) {
            pl.sendMessage(msg.replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specPlaceFailed");
        if (wp == null) {
            pl.sendMessage(msg.replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specDropFailed");
        if (wp == null) {
            pl.sendMessage(msg.replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        if (wp == null) {
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                e.setCancelled(true);
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    e.setCancelled(true);
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    e.setCancelled(true);
                }
            }
        }
    }


    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player pl = (Player) e.getDamager();
            if (pl.hasMetadata("NPC")) {
                return;
            }
            String w = e.getEntity().getLocation().getWorld().getName();
            WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            String msg = MessageManager.get().getMessage("specDamageFailed");
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp == null) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
                return;
            }
            PlayerType type = wp.getType();
            if (!wi.isJoinIfOwnerOnline()) {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (!WorldLister.checkOnline(wi)) {
                    if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
                else {
                    if (type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            Player pl = (Player) e.getRemover();
            String w = e.getEntity().getLocation().getWorld().getName();
            WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            String msg = MessageManager.get().getMessage("specDamageFailed");
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp == null) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
                return;
            }
            PlayerType type = wp.getType();
            if (!wi.isJoinIfOwnerOnline()) {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (!WorldLister.checkOnline(wi)) {
                    if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
                else {
                    if (type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specCommandFailed");
        if (wp == null) {
            pl.sendMessage(msg.replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                if (!(e.getMessage().equalsIgnoreCase("/spawn") || e.getMessage().equalsIgnoreCase("/back") || e.getMessage().indexOf("/home") == 0 || e.getMessage().indexOf("/wol") == 0 || e.getMessage().indexOf("/worldlist") == 0)) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    if (!(e.getMessage().equalsIgnoreCase("/spawn") || e.getMessage().equalsIgnoreCase("/back") || e.getMessage().indexOf("/home") == 0 || e.getMessage().indexOf("/wol") == 0 || e.getMessage().indexOf("/worldlist") == 0)) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    if (!(e.getMessage().equalsIgnoreCase("/spawn") || e.getMessage().equalsIgnoreCase("/back") || e.getMessage().indexOf("/home") == 0 || e.getMessage().indexOf("/wol") == 0 || e.getMessage().indexOf("/worldlist") == 0)) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player pl = (Player) e.getWhoClicked();
            String w = pl.getLocation().getWorld().getName();
            WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp == null) {
                e.setCancelled(true);
                return;
            }
            PlayerType type = wp.getType();
            if (!wi.isJoinIfOwnerOnline()) {
                if (type == PlayerType.SPECTATOR) {
                    e.setCancelled(true);
                }
            }
            else {
                if (!WorldLister.checkOnline(wi)) {
                    if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        e.setCancelled(true);
                    }
                }
                else {
                    if (type == PlayerType.SPECTATOR) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player pl = (Player) e.getEntity();
            if (pl.hasMetadata("NPC")) {
                return;
            }
            String w = pl.getLocation().getWorld().getName();
            WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp == null) {
                e.setCancelled(true);
                return;
            }
            PlayerType type = wp.getType();
            if (!wi.isJoinIfOwnerOnline()) {
                if (type == PlayerType.SPECTATOR) {
                    e.setCancelled(true);
                }
            }
            else {
                if (!WorldLister.checkOnline(wi)) {
                    if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        e.setCancelled(true);
                    }
                }
                else {
                    if (type == PlayerType.SPECTATOR) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        Player pl = e.getPlayer();
        if (pl.hasMetadata("NPC")) {
            return;
        }
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specDamageFailed");
        if (wp == null) {
            pl.sendMessage(msg.replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onVehicleDestroyEvent(VehicleDestroyEvent e) {
        if (e.getAttacker() instanceof Player) {
            Player pl = (Player) e.getAttacker();
            String w = pl.getLocation().getWorld().getName();
            WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            String msg = MessageManager.get().getMessage("specDamageFailed");
            if (wp == null) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
                return;
            }
            PlayerType type = wp.getType();
            if (!wi.isJoinIfOwnerOnline()) {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (!WorldLister.checkOnline(wi)) {
                    if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
                else {
                    if (type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHangingPlaceEvent(HangingPlaceEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
        if (wi == null) {
            return;
        }
        if (wi.getAcess() == WorldAccess.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specPlaceFailed");
        if (wp == null) {
            pl.sendMessage(msg.replace("&", "\u00a7"));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
            }
        }
        else {
            if (!WorldLister.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onHangingBreakEvent(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            Player pl = (Player) e.getRemover();
            String w = pl.getLocation().getWorld().getName();
            WorldInfo wi = WorldLister.getInstance().getWorldInfo(w);
            if (wi == null) {
                return;
            }
            if (wi.getAcess() == WorldAccess.PUBLIC) {
                return;
            }
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            String msg = MessageManager.get().getMessage("specBreakFailed");
            if (wp == null) {
                pl.sendMessage(msg.replace("&", "\u00a7"));
                e.setCancelled(true);
                return;
            }
            PlayerType type = wp.getType();
            if (!wi.isJoinIfOwnerOnline()) {
                if (type == PlayerType.SPECTATOR) {
                    pl.sendMessage(msg.replace("&", "\u00a7"));
                    e.setCancelled(true);
                }
            }
            else {
                if (!WorldLister.checkOnline(wi)) {
                    if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
                else {
                    if (type == PlayerType.SPECTATOR) {
                        pl.sendMessage(msg.replace("&", "\u00a7"));
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}

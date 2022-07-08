package ru.boomearo.worldlister.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
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
import ru.boomearo.worldlister.managers.ProtectedWorldManager;
import ru.boomearo.worldlister.objects.PlayerType;
import ru.boomearo.worldlister.objects.WorldAccessType;
import ru.boomearo.worldlister.objects.ProtectedWorld;
import ru.boomearo.worldlister.objects.WorldPlayer;

public class CheckListener implements Listener {

    private final ProtectedWorldManager protectedWorldManager;

    public CheckListener(ProtectedWorldManager protectedWorldManager) {
        this.protectedWorldManager = protectedWorldManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent e) {
        String w = e.getTo().getWorld().getName();
        Player pl = e.getPlayer();
        if (pl.hasMetadata("NPC")) {
            return;
        }
        ProtectedWorld wi = this.protectedWorldManager.getProtectedWorld(w);
        if (wi == null) {
            return;
        }
        if (e.getFrom().getWorld().equals(e.getTo().getWorld())) {
            return;
        }
        if (wi.getAccess() == WorldAccessType.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("teleportPerms");
        if (wp == null) {
            pl.sendMessage(msg.replace("%WORLD%", w));
            e.setCancelled(true);
            return;
        }
        PlayerType type = wp.getType();

        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                tpWorldClear(pl);
            }
        }
        else {
            if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                tpWorldClear(pl);
            }
        }
    }

    private static void tpWorldClear(Player pl) {
        pl.getInventory().clear();
        //TODO костыль, если нужно, можно придумать что то другое
        pl.chat("//none");
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        ProtectedWorld wi = this.protectedWorldManager.getProtectedWorld(w);
        if (wi == null) {
            return;
        }
        if (wi.getAccess() == WorldAccessType.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("joinPerms");
        if (wp == null) {
            pl.teleport(this.protectedWorldManager.getMainWorld().getSpawnLocation());
            pl.sendMessage(msg.replace("%WORLD%", w));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        Player pl = e.getPlayer();
        for (ProtectedWorld wi : this.protectedWorldManager.getAllProtectedWorlds()) {
            WorldPlayer wp = wi.getWorldPlayer(pl.getName());
            if (wp != null) {
                PlayerType type = wp.getType();
                if (type == PlayerType.OWNER) {
                    if (wi.isJoinIfOwnerOnline()) {
                        List<String> playerOnline = new ArrayList<>();
                        for (WorldPlayer wpa : wi.getAllWorldPlayers()) {
                            if (wpa.getType() == PlayerType.OWNER) {
                                if (WorldLister.getPlayerRight(wpa.getName()) != null) {
                                    playerOnline.add(wpa.getName());
                                }
                            }
                        }
                        if (playerOnline.size() <= 1) {
                            for (Player ppp : wi.getWorld().getPlayers()) {
                                String info1 = "&cПоследний владелец " + pl.getName() + " мира '" + wi.getName() + "' покинул сервер. Теперь у вас отсутствует доступ к модификации этого мира.";
                                ppp.sendMessage(info1);
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
        checkProtection(pl, e, "specBreakFailed");
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        Player pl = e.getPlayer();
        checkProtection(pl, e, "specPlaceFailed");
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        Player pl = e.getPlayer();
        checkProtection(pl, e, "specDropFailed");
    }


    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player pl = e.getPlayer();
        checkProtection(pl, e);
    }


    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Entity en = e.getDamager();
        if (en  instanceof Player) {
            Player pl = (Player) en;
            checkProtection(pl, e, "specDamageFailed");
        }
    }

    @EventHandler
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        Entity en = e.getRemover();
        if (en instanceof Player) {
            Player pl = (Player) en;
            checkProtection(pl, e, "specDamageFailed");
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
        Player pl = e.getPlayer();
        String w = pl.getLocation().getWorld().getName();
        ProtectedWorld wi = this.protectedWorldManager.getProtectedWorld(w);
        if (wi == null) {
            return;
        }
        if (wi.getAccess() == WorldAccessType.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = MessageManager.get().getMessage("specCommandFailed");
        if (wp == null) {
            if (isIllegalCommand(e.getMessage())) {
                pl.sendMessage(msg);
                e.setCancelled(true);
            }
            return;
        }

        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                if (isIllegalCommand(e.getMessage())) {
                    pl.sendMessage(msg);
                    e.setCancelled(true);
                }
            }
        }
        else {
            if (!this.protectedWorldManager.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    if (isIllegalCommand(e.getMessage())) {
                        pl.sendMessage(msg);
                        e.setCancelled(true);
                    }
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    if (isIllegalCommand(e.getMessage())) {
                        pl.sendMessage(msg);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    private static boolean isIllegalCommand(String message) {
        String[] args = message.split(" ");
        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase("/spawn") || args[0].equalsIgnoreCase("/back") || args[0].equalsIgnoreCase("/wol") || args[0].equalsIgnoreCase("/worldlist")) {
            return false;
        }
        return true;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        HumanEntity en = e.getWhoClicked();
        if (en instanceof Player) {
            Player pl = (Player) en;
            checkProtection(pl, e);
        }
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent e) {
        Entity en = e.getEntity();
        if (en instanceof Player) {
            Player pl = (Player) en;
            checkProtection(pl, e);
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        Player pl = e.getPlayer();
        checkProtection(pl, e, "specDamageFailed");
    }

    @EventHandler
    public void onVehicleDestroyEvent(VehicleDestroyEvent e) {
        Entity en = e.getAttacker();
        if (en instanceof Player) {
            Player pl = (Player) en;
            checkProtection(pl, e, "specDamageFailed");
        }
    }

    @EventHandler
    public void onHangingPlaceEvent(HangingPlaceEvent e) {
        Player pl = e.getPlayer();
        checkProtection(pl, e, "specPlaceFailed");
    }

    @EventHandler
    public void onHangingBreakEvent(HangingBreakByEntityEvent e) {
        Entity en = e.getRemover();
        if (en instanceof Player) {
            Player pl = (Player) en;
            checkProtection(pl, e, "specBreakFailed");
        }
    }

    private void checkProtection(Player pl, Cancellable e) {
        checkProtection(pl, e, null);
    }

    private void checkProtection(Player pl, Cancellable e, String cancelMessage) {
        if (pl.hasMetadata("NPC")) {
            return;
        }
        String worldName = pl.getLocation().getWorld().getName();
        ProtectedWorld wi = this.protectedWorldManager.getProtectedWorld(worldName);
        if (wi == null) {
            return;
        }
        if (wi.getAccess() == WorldAccessType.PUBLIC) {
            return;
        }
        WorldPlayer wp = wi.getWorldPlayer(pl.getName());
        String msg = cancelMessage;
        if (msg != null) {
            msg = MessageManager.get().getMessage(msg);
        }

        if (wp == null) {
            e.setCancelled(true);

            if (msg != null) {
                pl.sendMessage(msg);
            }
            return;
        }
        PlayerType type = wp.getType();
        if (!wi.isJoinIfOwnerOnline()) {
            if (type == PlayerType.SPECTATOR) {
                e.setCancelled(true);

                if (msg != null) {
                    pl.sendMessage(msg);
                }
            }
        }
        else {
            if (!this.protectedWorldManager.checkOnline(wi)) {
                if (type == PlayerType.MODER || type == PlayerType.MEMBER || type == PlayerType.SPECTATOR) {
                    e.setCancelled(true);

                    if (msg != null) {
                        pl.sendMessage(msg);
                    }
                }
            }
            else {
                if (type == PlayerType.SPECTATOR) {
                    e.setCancelled(true);

                    if (msg != null) {
                        pl.sendMessage(msg);
                    }
                }
            }
        }
    }
}

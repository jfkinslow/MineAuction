package net.amunak.bukkit.mineauction.sign;

/**
 * Copyright 2013 Jiří Barouš
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
import net.amunak.bukkit.mineauction.MineAuction;
import net.amunak.bukkit.mineauction.actions.VirtualInventory;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles all the interaction with MineAuction signs: their creation, removal
 * and player-interaction (right-clicking)
 *
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public final class SignInteractionListener implements Listener {

    protected MineAuction plugin;

    public SignInteractionListener(MineAuction p) {
        this.plugin = p;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && MineAuctionSign.isValidMineAuctionSign(event.getClickedBlock(), plugin)) {
            switch (plugin.getSignsStorage().getSignType((Sign) event.getClickedBlock().getState())) {
                case DEPOSIT:
                    VirtualInventory.deposit(event.getPlayer());
                    break;
                case WITHDRAW:
                    VirtualInventory.withdraw(event.getPlayer());
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (MineAuctionSign.isSign(event.getBlock())) {
            Sign sign = (Sign) event.getBlock().getState();
            if (sign.getLine(0).trim().equals(MineAuction.SIGN_IDENTIFIER)) {
                Player player = event.getPlayer();
                if (player.hasPermission("mineauction.signs.modify.place")) {
                    SignType type = SignType.getByName(sign.getLine(1).trim());
                    if (type == null) {
                        MineAuctionSign.invalidate(sign, "wrong type");
                    } else {
                        MineAuctionSign.handleCreation(sign, type, plugin);
                        plugin.log.info(player.getName() + " created a MineAuction sign of type '" + type.toString() + "' at " + sign.getLocation().toString());
                    }
                } else {
                    MineAuctionSign.invalidate(sign, "no permission");
                    plugin.log.info(player, "Sign creation failed:" + ChatColor.RED + " insufficient permission");
                    plugin.log.info(player.getName() + " tried to create interactive sign, but had no permission");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void blockBreakEvent(BlockBreakEvent event) {
        if (MineAuctionSign.isSign(event.getBlock())) {
            if (MineAuctionSign.isValidMineAuctionSign(event.getBlock(), plugin)) {
                Player player = event.getPlayer();
                if (player.hasPermission("mineauction.signs.modify.break")) {
                    Sign sign = (Sign) event.getBlock().getState();
                    MineAuctionSign.handleRemoval(sign, plugin);
                    plugin.log.info(player.getName() + " removed a MineAuction sign which was located at " + sign.getLocation().toString());
                } else {
                    event.setCancelled(true);
                    plugin.log.info(player, "Sign removal failed:" + ChatColor.RED + " insufficient permission");
                    plugin.log.info(player.getName() + " tried to remove interactive sign, but had no permission");
                }
            }
        }
    }
}
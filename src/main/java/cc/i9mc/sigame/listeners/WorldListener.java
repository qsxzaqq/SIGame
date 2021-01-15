package cc.i9mc.sigame.listeners;

import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.data.SIType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.UUID;

/**
 * Created by JinVan on 2021-01-14.
 */
public class WorldListener implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        UUID uuid = UUID.fromString(block.getWorld().getName().replace("_nether", ""));
        Player player = event.getPlayer();
        SIData siData = SIData.getSIData(uuid);

        if (uuid.equals(player.getUniqueId()) || siData.hasTrust(player.getName()) || siData.hasMember(player.getUniqueId())) {
            return;
        }

        if (block.getLocation().distance(siData.getSpawn().toLocation(block.getWorld())) < siData.getSize()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        UUID uuid = UUID.fromString(block.getWorld().getName().replace("_nether", ""));
        Player player = event.getPlayer();
        SIData siData = SIData.getSIData(uuid);

        if (uuid.equals(player.getUniqueId()) || siData.hasTrust(player.getName()) || siData.hasMember(player.getUniqueId())) {
            return;
        }

        if (block.getLocation().distance(siData.getSpawn().toLocation(block.getWorld())) < siData.getSize()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChange(WeatherChangeEvent event) {
        UUID uuid = UUID.fromString(event.getWorld().getName().replace("_nether", ""));
        SIData siData = SIData.getSIData(uuid);

        if (siData.getType() != SIType.WATER) {
            return;
        }

        siData.setRain(event.toWeatherState());
    }
}

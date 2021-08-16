package cc.i9mc.sigame.listeners;

import cc.i9mc.sigame.data.SIData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.weather.WeatherChangeEvent;


/**
 * Created by JinVan on 2021-01-14.
 */
public class WorldListener implements Listener {

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        SIData islandData = SIData.getByWorld(player.getWorld());
        SIData playerData = SIData.get(player.getUniqueId());

        if (islandData == null || playerData == null) {
            event.setCancelled(true);
            return;
        }

        if (islandData.equals(playerData)) {
            return;
        }

        if (block.getLocation().distance(islandData.getSpawn().toLocation(islandData.getWorld())) < islandData.getSize()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        SIData islandData = SIData.getByWorld(player.getWorld());
        SIData playerData = SIData.get(player.getUniqueId());

        if (islandData == null || playerData == null) {
            event.setCancelled(true);
            return;
        }

        if (islandData.equals(playerData)) {
            return;
        }

        if (block.getLocation().distance(islandData.getSpawn().toLocation(block.getWorld())) < islandData.getSize()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChangeWeather(WeatherChangeEvent event) {
        SIData siData = SIData.getByWorld(event.getWorld());

        if (siData == null) {
            return;
        }

        if (siData.getType() != SIData.GameType.WATER) {
            return;
        }

        siData.setRain(event.toWeatherState());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        SIData siData = SIData.getByWorld(event.getPlayer().getWorld());

        if (siData == null) {
            event.getPlayer().kickPlayer("非法操作");
        }
    }
}

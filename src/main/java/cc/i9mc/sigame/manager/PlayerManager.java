package cc.i9mc.sigame.manager;

import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.Database;
import cc.i9mc.sigame.data.GameJoin;
import cc.i9mc.sigame.data.GamePlayer;
import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.utils.BorderUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by JinVan on 2021-01-14.
 */
public class PlayerManager {
    private static final Cache<UUID, GameJoin> dataCache = CacheBuilder.newBuilder().maximumSize(Bukkit.getMaxPlayers()).expireAfterWrite(30, TimeUnit.SECONDS).build();

    public void load(GameJoin gameJoin) {
        Database.executorService.execute(() -> {
            SIData islandData = SIData.get(gameJoin.getUuid());
            SIData playerData = SIData.get(gameJoin.getPlayer());

            if (islandData == null && playerData == null) {
                islandData = SIData.load(gameJoin.getUuid());
                playerData = SIData.get(gameJoin.getPlayer());
            }

            if (islandData == null || playerData == null) {
                return;
            }

            if (islandData.equals(playerData) && !islandData.isLoadWorld()) {
                islandData.updateLock(true);
                SIGame.getInstance().getWorldManager().loadPlayer(islandData);
            }

            GamePlayer gamePlayer = new GamePlayer();
            gamePlayer.setUuid(gameJoin.getPlayer());
            gamePlayer.setIslandUuid(gameJoin.getUuid());
            gamePlayer.setPlayerType(islandData.getPlayer(gamePlayer.getUuid()).getPlayerType());
            GamePlayer.init(gamePlayer);

            Player player = Bukkit.getPlayer(gameJoin.getPlayer());
            if (player != null) {
                SIData finalIslandData = islandData;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerJoin(player, finalIslandData);
                    }
                }.runTask(SIGame.getInstance());
            } else {
                dataCache.put(gameJoin.getPlayer(), gameJoin);
            }
        });
    }

    public GameJoin get(UUID uuid) {
        return dataCache.getIfPresent(uuid);
    }

    public void playerJoin(Player player, SIData siData) {
        Database.executorService.execute(() -> Database.updateMemberLastTime(player.getUniqueId()));
        player.teleport(siData.getSpawn().toLocation(siData.getWorld()));

        new BukkitRunnable() {
            @Override
            public void run() {
                refreshPlayers(siData);
                BorderUtil.sendBorder(player, siData.getBorderColor(), siData.getSize());
            }
        }.runTaskLater(SIGame.getInstance(), 5L);
    }

    public void refreshPlayers(SIData siData) {
        List<Player> members = siData.getOnlinePlayers();

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player player1 : members) {
                if (!player1.getUniqueId().equals(player.getUniqueId())) {
                    members.forEach(member -> player.hidePlayer(SIGame.getInstance(), member));
                    members.forEach(member -> member.hidePlayer(SIGame.getInstance(), player));
                }
            }
        }

        members.forEach(member -> members.forEach(player -> player.showPlayer(SIGame.getInstance(), member)));
    }
}

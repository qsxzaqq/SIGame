package cc.i9mc.sigame.manager;

import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.Database;
import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.data.SIGameDeny;
import cc.i9mc.sigame.data.SIGameJoin;
import cc.i9mc.sigame.utils.BorderUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by JinVan on 2021-01-14.
 */
public class PlayerManager {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final Cache<UUID, SIGameJoin> dataCache = CacheBuilder.newBuilder().maximumSize(Bukkit.getMaxPlayers()).expireAfterWrite(30, TimeUnit.SECONDS).build();
    public static final ConcurrentHashMap<UUID, UUID> PLAYER = new ConcurrentHashMap<>();

    public void addData(SIGameJoin siGameJoin) {
        executorService.execute(() -> {
            SIData siData = null;
            if (!SIData.DATA.containsKey(siGameJoin.getUuid())) {
                if (siGameJoin.getJoinType() == SIGameJoin.JoinType.MEMBER) {
                    siData = SIData.getSIData(siGameJoin.getUuid());
                    if (siData == null) {
                        return;
                    }
                    siData.updateLock(true);
                    SIGame.getInstance().getWorldManager().loadPlayer(siGameJoin.getUuid(), siData.getType(), siData.getDifficulty());
                }
            }

            Player player = Bukkit.getPlayer(siGameJoin.getPlayer());
            if (player != null) {
                if (playerLogin(player, siGameJoin, siData) == SIGameDeny.ALLOW) {
                    SIData finalSiData = siData;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            playerJoin(player, siGameJoin, finalSiData);
                        }
                    }.runTask(SIGame.getInstance());
                }
            } else {
                dataCache.put(siGameJoin.getPlayer(), siGameJoin);
            }
        });
    }

    public SIGameJoin getData(UUID uuid) {
        return dataCache.getIfPresent(uuid);
    }

    public SIGameDeny playerLogin(Player player, SIGameJoin siGameJoin, SIData siData) {
        if (siGameJoin == null || siData == null) {
            return SIGameDeny.DATA_NULL;
        }

        if (siGameJoin.getJoinType() == SIGameJoin.JoinType.DEFAULT && siData.getWarp() == null) {
            return SIGameDeny.WARP_NULL;
        }

        PLAYER.put(player.getUniqueId(), siGameJoin.getUuid());
        return SIGameDeny.ALLOW;
    }

    public void playerJoin(Player player, SIGameJoin siGameJoin, SIData siData) {
        switch (siGameJoin.getJoinType()) {
            case MEMBER:
                executorService.execute(() -> Database.updateMemberLastTime(player.getUniqueId()));
                player.teleport(siData.getSpawn().toLocation(Bukkit.getWorld(siData.getUuid().toString())));
                break;
            case TRUST:
                executorService.execute(() -> Database.updateTrustLastTime(player.getUniqueId()));
                player.teleport(siData.getSpawn().toLocation(Bukkit.getWorld(siData.getUuid().toString())));
                break;
            case DEFAULT:
                player.teleport(siData.getWarp().toLocation(Bukkit.getWorld(siData.getUuid().toString())));
                break;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                resetPlayer(siData);
                BorderUtil.sendBorder(player, siData.getBorderColor(), siData.getSize());
            }
        }.runTaskLater(SIGame.getInstance(), 5L);
    }

    public void resetPlayer(SIData siData) {
        List<Player> members = new ArrayList<>(Bukkit.getWorld(siData.getUuid().toString()).getPlayers());
        members.addAll(Bukkit.getWorld(siData.getUuid().toString() + "_nether").getPlayers());

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

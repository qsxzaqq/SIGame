package cc.i9mc.sigame.api;

import cc.i9mc.sigame.data.SIData;
import com.google.gson.Gson;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

/**
 * Created by JinVan on 2021-01-13.
 */
public class SIAPI {
    private static final Gson GSON = new Gson();

    public static boolean inIsland(Player player) {
        SIData siData = SIData.getByWorld(player.getWorld());
        SIData playerData = SIData.get(player.getUniqueId());

        return siData != null && siData.equals(playerData);
    }

    public static boolean isOwner(Player player) {
        if (!inIsland(player)) {
            return false;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return false;
        }

        return siData.getOwner().getUuid().equals(player.getUniqueId());
    }

    public static SIData.Player.PlayerType getPlayerType(Player player) {
        if (!inIsland(player)) {
            return null;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return null;
        }

        return siData.getPlayer(player.getUniqueId()).getPlayerType();
    }

    public static boolean isPVP(Player player) {
        if (!inIsland(player)) {
            return false;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return false;
        }

        return siData.isPvp();
    }

    public static void setPVP(Player player, boolean pvp) {
        if (!inIsland(player)) {
            return;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return;
        }

        siData.updatePVP(pvp);
    }

    public static String getBorderColor(Player player) {
        if (!inIsland(player)) {
            return null;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return null;
        }

        return siData.getBorderColor().toString();
    }

    public static void setBorderColor(Player player, String borderColor) {
        if (!inIsland(player)) {
            return;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return;
        }

        siData.updateBorderColor(SIData.BorderColor.valueOf(borderColor));
    }

    public static boolean isOpen(Player player) {
        if (!inIsland(player)) {
            return false;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return false;
        }

        return siData.isOpen();
    }

    public static void setOpen(Player player, boolean pvp) {
        if (!inIsland(player)) {
            return;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return;
        }

        siData.updateOpen(pvp);
    }

    public static SIData.GameBiome getBiome(Player player) {
        if (!inIsland(player)) {
            return null;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return null;
        }


        return SIData.GameBiome.getByBiome(siData.getBiome());
    }

    public static void setBiome(Player player, String biome) {
        if (!inIsland(player)) {
            return;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return;
        }

        siData.updateBiome(Biome.valueOf(biome));
    }

    public static void setSpawn(Player player, org.bukkit.Location location) {
        if (!inIsland(player)) {
            return;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return;
        }

        siData.updateSpawn(location);
    }

    public static void setWarp(Player player, org.bukkit.Location location) {
        if (!inIsland(player)) {
            return;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return;
        }

        siData.updateWarp(location);
    }

    public static SIData.Player[] getMembers(Player player) {
        if (!inIsland(player)) {
            return null;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return null;
        }

        return siData.getMembers().toArray(new SIData.Player[0]);
    }

    public static SIData.Player[] getTrusts(Player player) {
        if (!inIsland(player)) {
            return null;
        }

        SIData siData = SIData.get(player.getUniqueId());
        if (siData == null) {
            return null;
        }

        return siData.getTrusts().toArray(new SIData.Player[0]);
    }
/*
    public static void requestMember(Player player, String target) {
        if (!SIData.DATA.containsKey(player.getUniqueId()) || !Database.hasData(player.getUniqueId())) {
            player.sendMessage("§c请先创建岛屿");
        }

        if (Database.hasData(target)) {
            player.sendMessage("§c该玩家拥有岛屿");
        }

        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setUuid(player.getUniqueId());
        memberRequest.setName(player.getName());
        memberRequest.setTarget(target);

        player.sendMessage("§a请求已发送");

        new BukkitRunnable() {
            @Override
            public void run() {
                JedisUtil.publish("SI.Game.MemberRequest", GSON.toJson(memberRequest));
            }
        }.runTaskAsynchronously(SIGame.getInstance());
    }

    public static void addTrust(Player player, String target) {
        if (!SIData.DATA.containsKey(player.getUniqueId()) || !Database.hasData(player.getUniqueId())) {
            player.sendMessage("§c请先创建岛屿");
        }

        if (SIData.DATA.containsKey(player.getUniqueId()) && SIData.DATA.get(player.getUniqueId()).hasTrust(target) || Database.hasTrust(player.getUniqueId(), target)) {
            player.sendMessage("§c该玩家拥有权限");
        }

        SIData.Player player1 = new SIData.Player();
        player1.setUuid(null);
        player1.setName(target);
        player1.setJoinTime(new Date());
        player1.setLastTime(null);

        if (SIData.DATA.containsKey(player.getUniqueId())) {
            SIData.DATA.get(player.getUniqueId()).getTrusts().add(player1);
        }
        Database.addTrust(player.getUniqueId(), player1);
    }

*/
}

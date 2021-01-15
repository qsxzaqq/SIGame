package cc.i9mc.sigame.api;

import cc.i9mc.gameutils.utils.JedisUtil;
import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.Database;
import cc.i9mc.sigame.data.MemberRequest;
import cc.i9mc.sigame.data.SIData;
import com.google.gson.Gson;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.UUID;

/**
 * Created by JinVan on 2021-01-13.
 */
public class SIAPI {
    private static final Gson GSON = new Gson();

 /*   public static String getBorderColor(String uuid) {
      return SIData.getSIData(UUID.fromString(uuid)).getBorder().getBorderColor().toString();
    }

    public static void setBorderColor(String uuid, String color) {
        SIData siData = SIData.getSIData(UUID.fromString(uuid));
        siData.getBorder().setBorderColor(Border.BorderColor.valueOf(color));

        siData.saveBorder();
    }

    public static double getBorderSize(String uuid) {
        return SIData.getSIData(UUID.fromString(uuid)).getBorder().getSize();
    }

    public static void setBorderSize(String uuid, double size) {
        SIData siData = SIData.getSIData(UUID.fromString(uuid));
        siData.getBorder().setSize(size);

        siData.saveBorder();
    }
*/
    public static String getBiome(String uuid) {
        return SIData.getSIData(UUID.fromString(uuid)).getBiome().toString();
    }

    public static void setBiome(String uuid, String biome) {
        SIData.getSIData(UUID.fromString(uuid)).updateBiome(Biome.valueOf(biome));
    }

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

    public void setSpawn(Player player, org.bukkit.Location location) {
        if (!SIData.DATA.containsKey(player.getUniqueId()) || !Database.hasData(player.getUniqueId())) {
            player.sendMessage("§c请先创建岛屿");
        }

        if (!player.getWorld().getName().startsWith(player.getUniqueId().toString())) {
            return;
        }
    }

    public void updateWarp(org.bukkit.Location location) {
    }
}

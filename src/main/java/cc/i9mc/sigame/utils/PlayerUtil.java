package cc.i9mc.sigame.utils;

import cc.i9mc.sigame.SIGame;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

/**
 * Created by JinVan on 2021-01-23.
 */
public class PlayerUtil {
    public static void kill(CraftPlayer cp, String value, String signature) {
        double health = cp.getHealth();
        Location location = cp.getLocation().add(0, 1, 0);
        new BukkitRunnable() {
            public void run() {
                GameProfile profile = cp.getProfile();

                profile.getProperties()
                        .put("textures", new Property("textures", value, signature));

                Collection<Property> prop = profile.getProperties().get("textures");
                cp.getProfile().getProperties().putAll("textures", prop);
                PacketPlayOutEntityDestroy pds = new PacketPlayOutEntityDestroy(
                        cp.getEntityId());
                sendPacket(pds);
                PacketPlayOutPlayerInfo tab = new PacketPlayOutPlayerInfo(
                        PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                        cp.getHandle());
                sendPacket(tab);
                cp.setHealth(0D);
            }
        }.runTaskLater(SIGame.getInstance(), 1L);
        new BukkitRunnable() {
            public void run() {
                cp.spigot().respawn();
                cp.setHealth(health);
                cp.teleport(location);
                PacketPlayOutPlayerInfo tabadd = new PacketPlayOutPlayerInfo(
                        PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
                        cp.getHandle());
                sendPacket(tabadd);
                PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(
                        cp.getHandle());
                for (Player pls : Bukkit.getOnlinePlayers()) {
                    if (!pls.getName().equals(cp.getName())) {
                        ((CraftPlayer) pls).getHandle().playerConnection
                                .sendPacket(spawn);
                    }
                }
            }
        }.runTaskLater(SIGame.getInstance(), 5L);
    }

    private static void sendPacket(Packet packet) {
        for (Player pls : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) pls).getHandle().playerConnection.sendPacket(packet);
        }
    }
}

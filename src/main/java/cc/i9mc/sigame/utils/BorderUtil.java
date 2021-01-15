package cc.i9mc.sigame.utils;

import cc.i9mc.sigame.data.BorderColor;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.WorldBorder;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Created by JinVan on 2021-01-13.
 */
public class BorderUtil {

/*    @SneakyThrows
    public static void clearBorder(Player player, Border border) {
        Border rawBorder = border.clone();
        rawBorder.setSize(1.4999992E7D);
        sendBorder(player, rawBorder);
    }*/

    public static void sendBorder(Player player, BorderColor borderColor, int size) {
        WorldBorder worldBorder = new WorldBorder();
        worldBorder.setCenter(0, 0);
        worldBorder.setSize(size);
        worldBorder.setWarningDistance(0);

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        worldBorder.world = (WorldServer)entityPlayer.world;

        switch (borderColor) {
            case BLUE:
                setBorder(entityPlayer, worldBorder);
                break;
            case RED:
                setBorder(entityPlayer, worldBorder);
                worldBorder.transitionSizeBetween(size, size - 1.0D, 20000000L);
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.LERP_SIZE));
                break;
            case GREEN:
                setBorder(entityPlayer, worldBorder);
                worldBorder.transitionSizeBetween(size - 0.2D, size - 0.1D + 0.1D, 20000000L);
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.LERP_SIZE));
                break;
        }
    }

    private static void setBorder(EntityPlayer entityPlayer, WorldBorder worldBorder) {
        PacketPlayOutWorldBorder sizePacket = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE);
        entityPlayer.playerConnection.sendPacket(sizePacket);
        PacketPlayOutWorldBorder centerPacket = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER);
        entityPlayer.playerConnection.sendPacket(centerPacket);
        PacketPlayOutWorldBorder warning = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS);
        entityPlayer.playerConnection.sendPacket(warning);
    }
}

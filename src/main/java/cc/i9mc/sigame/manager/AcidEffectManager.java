package cc.i9mc.sigame.manager;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by JinVan on 2021-01-14.
 */
public class AcidEffectManager {

    public boolean isSafeFromRain(Player player) {
        if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType().name().contains("HELMET")) {
            return true;
        }

        for (PotionEffect s : player.getActivePotionEffects()) {
            if (s.getType().equals(PotionEffectType.WATER_BREATHING)) {
                return true;
            }
        }

        for (int y = player.getLocation().getBlockY() + 2; y < player.getLocation().getWorld().getMaxHeight(); y++) {
            if (!player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ()).getType().equals(Material.AIR)) {
                return true;
            }
        }

        return false;
    }

    public boolean isSafeFromAcid(Player player) {
        Material bodyMat = player.getLocation().getBlock().getType();
        Material headMat = player.getLocation().getBlock().getRelative(BlockFace.UP).getType();

        if (bodyMat.equals(Material.STATIONARY_WATER)) {
            bodyMat = Material.WATER;
        }
        if (headMat.equals(Material.STATIONARY_WATER)) {
            headMat = Material.WATER;
        }
        if (bodyMat != Material.WATER && headMat != Material.WATER) {
            return true;
        }

        Entity playersVehicle = player.getVehicle();
        if (playersVehicle != null) {
            if (playersVehicle.getType().equals(EntityType.BOAT)) {
                return true;
            }
        }

        for (PotionEffect s : player.getActivePotionEffects()) {
            if (s.getType().equals(PotionEffectType.WATER_BREATHING)) {
                return true;
            }
        }

        return false;
    }

    public double getDamageReduced(Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();
        double red = 0.0;

        if (helmet != null) {
            if (helmet.getType() == Material.LEATHER_HELMET)
                red = red + 0.04;
            else if (helmet.getType() == Material.GOLD_HELMET)
                red = red + 0.08;
            else if (helmet.getType() == Material.CHAINMAIL_HELMET)
                red = red + 0.08;
            else if (helmet.getType() == Material.IRON_HELMET)
                red = red + 0.08;
            else if (helmet.getType() == Material.DIAMOND_HELMET)
                red = red + 0.12;
        }

        if (boots != null) {
            if (boots.getType() == Material.LEATHER_BOOTS)
                red = red + 0.04;
            else if (boots.getType() == Material.GOLD_BOOTS)
                red = red + 0.04;
            else if (boots.getType() == Material.CHAINMAIL_BOOTS)
                red = red + 0.04;
            else if (boots.getType() == Material.IRON_BOOTS)
                red = red + 0.08;
            else if (boots.getType() == Material.DIAMOND_BOOTS)
                red = red + 0.12;
        }

        // Pants
        if (pants != null) {
            if (pants.getType() == Material.LEATHER_LEGGINGS)
                red = red + 0.08;
            else if (pants.getType() == Material.GOLD_LEGGINGS)
                red = red + 0.12;
            else if (pants.getType() == Material.CHAINMAIL_LEGGINGS)
                red = red + 0.16;
            else if (pants.getType() == Material.IRON_LEGGINGS)
                red = red + 0.20;
            else if (pants.getType() == Material.DIAMOND_LEGGINGS)
                red = red + 0.24;
        }

        // Chest plate
        if (chest != null) {
            if (chest.getType() == Material.LEATHER_CHESTPLATE)
                red = red + 0.12;
            else if (chest.getType() == Material.GOLD_CHESTPLATE)
                red = red + 0.20;
            else if (chest.getType() == Material.CHAINMAIL_CHESTPLATE)
                red = red + 0.20;
            else if (chest.getType() == Material.IRON_CHESTPLATE)
                red = red + 0.24;
            else if (chest.getType() == Material.DIAMOND_CHESTPLATE)
                red = red + 0.32;
        }

        return red;
    }
}

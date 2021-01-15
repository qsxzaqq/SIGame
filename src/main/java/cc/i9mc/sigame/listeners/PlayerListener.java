package cc.i9mc.sigame.listeners;

import cc.i9mc.gameutils.utils.ItemSerializerUtil;
import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.data.SIGameDeny;
import cc.i9mc.sigame.data.SIGameJoin;
import cc.i9mc.sigame.data.SIType;
import com.google.gson.Gson;
import com.meowj.langutils.lang.LanguageHelper;
import com.meowj.langutils.locale.LocaleHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by JinVan on 2021-01-14.
 */
public class PlayerListener implements Listener {
    private final List<Player> burningPlayers = new ArrayList<>();
    private final List<Player> wetPlayers = new ArrayList<>();
    private final List<Player> noDamagePlayers = new ArrayList<>();

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        SIGameJoin siGameJoin = SIGame.getInstance().getPlayerManager().getData(player.getUniqueId());
        if (siGameJoin == null) {
            event.disallow(PlayerLoginEvent.Result.KICK_FULL, SIGameDeny.DATA_NULL.getMessage());
            return;
        }

        SIData siData = SIData.DATA.getOrDefault(siGameJoin.getUuid(), null);
        SIGameDeny siGameDeny = SIGame.getInstance().getPlayerManager().playerLogin(player, siGameJoin, siData);
        if (siGameDeny == SIGameDeny.ALLOW) {
            event.allow();
            return;
        }

        event.disallow(PlayerLoginEvent.Result.KICK_FULL, siGameDeny.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SIGameJoin siGameJoin = SIGame.getInstance().getPlayerManager().getData(player.getUniqueId());
        SIData siData = SIData.DATA.get(siGameJoin.getUuid());

        event.setJoinMessage(null);
        SIGame.getInstance().getPlayerManager().playerJoin(player, siGameJoin, siData);

        noDamagePlayers.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                noDamagePlayers.remove(player);
            }
        }.runTaskLater(SIGame.getInstance(), 80L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && noDamagePlayers.contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setKeepInventory(true);

        Player player = event.getEntity();
        UUID uuid = UUID.fromString(player.getWorld().getName().replace("_nether", ""));
        SIData siData = SIData.getSIData(uuid);
        EntityDamageEvent.DamageCause damageCause = event.getEntity().getLastDamageCause().getCause();

        switch (damageCause) {
            case ENTITY_ATTACK:
                Player killer = player.getKiller();
                if (killer != null) {
                    TextComponent textComponent = new TextComponent("§a" + player.getName() + " §7被 §a" + killer.getName() + " ");

                    ItemStack itemStack = killer.getItemInHand();

                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        String name = null;
                        if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getDisplayName() != null) {
                            name = itemStack.getItemMeta().getDisplayName();
                        } else {
                            name = LanguageHelper.getItemDisplayName(itemStack, "zh_cn");
                        }
                        textComponent.addExtra("§7使用 §f");
                        textComponent.addExtra(name);
                        textComponent.addExtra(" §7击杀");
                        textComponent.getExtra().get(1).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(ItemSerializerUtil.getItemStackJson(itemStack)).create()));
                    } else {
                        textComponent.addExtra("§7击杀");
                    }

                    siData.sendMessage(textComponent);
                }
                break;
            case FIRE:
            case FIRE_TICK:
            case LAVA:
                siData.sendMessage("§a" + player.getName() + " §7烫到jio了");
                break;
            case POISON:
                siData.sendMessage("§a" + player.getName() + " §7被魔法打败了");
                break;
            case FALLING_BLOCK:
            case DROWNING:
                siData.sendMessage("§a" + player.getName() + " §7呛死了");
                break;
            case FALL:
                siData.sendMessage("§a" + player.getName() + " §7帅死了");
                break;
            case SUICIDE:
                siData.sendMessage("§a" + player.getName() + " §7自身自灭");
                break;
            case VOID:
                siData.sendMessage("§a" + player.getName() + " §7坠入虚空");
                break;
            default:
                siData.sendMessage("§a" + player.getName() + " §7不知道咋阵亡了");
                break;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                player.spigot().respawn();
            }
        }.runTaskLater(SIGame.getInstance(), 10L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = UUID.fromString(player.getWorld().getName().replace("_nether", ""));
        SIData siData = SIData.getSIData(uuid);

        event.setRespawnLocation(siData.getSpawn().toLocation(player.getWorld()));

        noDamagePlayers.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                noDamagePlayers.remove(player);
            }
        }.runTaskLater(SIGame.getInstance(), 80L);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            UUID uuid = UUID.fromString(event.getEntity().getWorld().getName().replace("_nether", ""));
            SIData siData = SIData.getSIData(uuid);

            if (!siData.isPvp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        UUID uuid = UUID.fromString(event.getPlayer().getWorld().getName().replace("_nether", ""));
        Player player = event.getPlayer();
        SIData siData = SIData.getSIData(uuid);

        if (siData.getType() != SIType.WATER) {
            return;
        }

        if (player.isDead() || player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        final Location location = player.getLocation();

        if (siData.isRain()) {
            Biome biome = location.getBlock().getBiome();
            if (biome != Biome.DESERT && biome != Biome.DESERT_HILLS && biome != Biome.SAVANNA && biome != Biome.MESA && biome != Biome.HELL) {
                if (SIGame.getInstance().getAcidEffectManager().isSafeFromRain(player)) {
                    wetPlayers.remove(player);
                } else {
                    if (!wetPlayers.contains(player)) {
                        wetPlayers.add(player);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!siData.isRain() || player.isDead() || SIGame.getInstance().getAcidEffectManager().isSafeFromRain(player)) {
                                    wetPlayers.remove(player);
                                    cancel();
                                } else {
                                    double damage = (1 - SIGame.getInstance().getAcidEffectManager().getDamageReduced(player));
                                    player.damage(damage);
                                    player.getWorld().playSound(location, Sound.ENTITY_CREEPER_PRIMED, 3F, 3F);
                                }
                            }
                        }.runTaskTimer(SIGame.getInstance(), 0L, 20L);
                    }
                }
            }
        }

        if (location.getBlockY() < 1) {
            player.setVelocity(new Vector(player.getVelocity().getX(), 1D, player.getVelocity().getZ()));
        }

        if (burningPlayers.contains(player)) {
            return;
        }

        if (SIGame.getInstance().getAcidEffectManager().isSafeFromAcid(player)) {
            return;
        }

        burningPlayers.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isDead() || SIGame.getInstance().getAcidEffectManager().isSafeFromAcid(player)) {
                    burningPlayers.remove(player);
                    this.cancel();
                } else {
                    double damage = 5 - 5 * SIGame.getInstance().getAcidEffectManager().getDamageReduced(player);

                    for (PotionEffectType t : new PotionEffectType[]{PotionEffectType.WEAKNESS, PotionEffectType.SLOW}) {
                        if (t.equals(PotionEffectType.BLINDNESS) || t.equals(PotionEffectType.CONFUSION) || t.equals(PotionEffectType.HUNGER) || t.equals(PotionEffectType.SLOW) || t.equals(PotionEffectType.SLOW_DIGGING) || t.equals(PotionEffectType.WEAKNESS)) {
                            player.addPotionEffect(new PotionEffect(t, 600, 1));
                        } else {
                            player.addPotionEffect(new PotionEffect(t, 200, 1));
                        }
                    }

                    if (damage > 0D) {
                        player.damage(damage);
                        player.getWorld().playSound(location, Sound.ENTITY_CREEPER_PRIMED, 3F, 3F);
                    }
                }
            }
        }.runTaskTimer(SIGame.getInstance(), 0L, 20L);
    }
}

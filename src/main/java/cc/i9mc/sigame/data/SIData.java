package cc.i9mc.sigame.data;

import cc.i9mc.gameutils.BukkitGameUtils;
import cc.i9mc.gameutils.utils.ItemBuilderUtil;
import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.tasks.SetBiomeTask;
import cc.i9mc.sigame.utils.BorderUtil;
import cc.i9mc.sigame.utils.PlayerUtil;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.EnumDifficulty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JinVan on 2021-01-13.
 */
@Data
public class SIData {
    public static ConcurrentHashMap<Integer, SIData> DATA = new ConcurrentHashMap<>();
    private static Gson GSON = new Gson();

    private Integer id;
    private Player owner;
    private GameType type;
    private boolean pvp;
    private BorderColor borderColor;
    private int size;
    private boolean open;
    private Biome biome;

    private boolean lock;
    private List<Player> members;
    private List<Player> trusts;

    private Location spawn;
    private Location warp;

    private long lastTime;
    private boolean rain = false;

    public static SIData load(UUID uuid) {
        SIData siData = Database.load(uuid);
        if (siData != null) {
            DATA.put(siData.getId(), siData);
        }

        return siData;
    }

    public static SIData get(UUID uuid) {
        for (Map.Entry<Integer, SIData> entry : DATA.entrySet()) {
            if (entry.getValue().getOwner().getUuid().equals(uuid)) {
                return entry.getValue();
            }

            for (Player player : entry.getValue().getMembers()) {
                if (player.getUuid().equals(uuid)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    public static SIData getByWorld(World world) {
        int id;
        try {
            id = Integer.parseInt(world.getName().replace("_nether", ""));
        } catch (Exception ignored) {
            return null;
        }

        return DATA.getOrDefault(id, null);
    }

    public void sendMessage(String message) {
        getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    public void sendMessage(TextComponent message) {
        getOnlinePlayers().forEach(player -> player.spigot().sendMessage(message));
    }

    public List<org.bukkit.entity.Player> getOnlinePlayers() {
        List<org.bukkit.entity.Player> members = new ArrayList<>(getWorld().getPlayers());
        members.addAll(getNetherWorld().getPlayers());

        return members;
    }

    public boolean hasMember(String name) {
        for (Player trust : members) {
            if (trust.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMember(UUID uuid) {
        for (Player trust : members) {
            if (trust.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTrust(UUID uuid) {
        for (Player trust : trusts) {
            if (trust.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTrust(String name) {
        for (Player trust : trusts) {
            if (trust.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Player getPlayer(UUID uuid) {
        if (owner.getUuid().equals(uuid)) {
            return owner;
        }

        for (Player player : members) {
            if (player.getUuid().equals(uuid)) {
                return player;
            }
        }

        return null;
    }

    public void updatePVP(boolean pvp) {
        this.pvp = pvp;

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.pvp = ? WHERE si_data.id = ?");
                preparedStatement.setBoolean(1, pvp);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateOpen(boolean open) {
        this.open = open;

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.open = ? WHERE si_data.id = ?");
                preparedStatement.setBoolean(1, pvp);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateBorderColor(BorderColor borderColor) {
        this.borderColor = borderColor;

        for (org.bukkit.entity.Player player : getOnlinePlayers()) {
            BorderUtil.sendBorder(player, borderColor, size);
        }

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.border_color = ? WHERE si_data.id = ?");
                preparedStatement.setString(1, borderColor.toString());
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateSize(int size) {
        this.size = size;

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.size = ? WHERE si_data.id = ?");
                preparedStatement.setInt(1, size);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateBiome(Biome biome) {
        if(this.biome.equals(biome)) {
            return;
        }

        this.biome = biome;
        new BukkitRunnable() {
            @Override
            public void run() {
                new SetBiomeTask(SIData.this).run();
            }
        }.runTaskAsynchronously(SIGame.getInstance());

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.biome = ? WHERE si_data.id = ?");
                preparedStatement.setString(1, this.biome.toString());
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateLock(boolean lock) {
        this.lock = lock;

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.lock = ? WHERE si_data.id = ?");
                preparedStatement.setBoolean(1, lock);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateSpawn(org.bukkit.Location location) {
        this.spawn = Location.createLocation(location);

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET spawn = ? WHERE id = ?");
                preparedStatement.setString(1, GSON.toJson(this.spawn));
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateWarp(org.bukkit.Location location) {
        this.warp = Location.createLocation(location);

        Database.executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET warp = ? WHERE id = ?");
                preparedStatement.setString(1, GSON.toJson(this.warp));
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public World getWorld() {
        return Bukkit.getWorld(id.toString());
    }

    public World getNetherWorld() {
        return Bukkit.getWorld(id.toString() + "_nether");
    }

    public boolean equals(Object obj) {
        if (obj instanceof SIData) {
            return id.equals(((SIData) obj).getId());
        }
        return false;
    }

    public boolean isLoadWorld() {
        return getWorld() != null && getNetherWorld() != null;
    }

    public void update() {
        SIData siData = Database.loadByData(getOwner().getUuid());
        if (siData == null) {
            return;
        }
        type = siData.getType();
        pvp = siData.isPvp();
        borderColor = siData.getBorderColor();
        size = siData.getSize();
        open = siData.isOpen();
        biome = siData.getBiome();
        members = siData.getMembers();
        trusts = siData.getTrusts();
    }

    public enum BorderColor {
        RED, GREEN, BLUE
    }

    public enum GameType {
        SKY, WATER
    }

    @Data
    @AllArgsConstructor
    public static class Location {
        private String world;
        private double x;
        private double y;
        private double z;
        private float pitch;
        private float yaw;

        public static Location createLocation(org.bukkit.Location location) {
            return new Location(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        }

        public org.bukkit.Location toLocation() {
            return new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, pitch, yaw);
        }

        public org.bukkit.Location toLocation(World world) {
            return new org.bukkit.Location(world, x, y, z, pitch, yaw);
        }
    }

    @Data
    public static class Player {
        private int id;
        private UUID uuid;
        private String name;
        private PlayerType playerType;
        private Date joinTime;
        private Date lastTime;

        public org.bukkit.entity.Player getPlayer() {
            return Bukkit.getPlayer(uuid);
        }

        public boolean isOnline() {
            return getPlayer() != null;
        }

        public enum PlayerType {
            OWNER("岛主"), ADMIN("管理员"), DEFAULT("无");

            @Getter
            private final String name;

            PlayerType(String name) {
                this.name = name;
            }
        }
    }

    public enum GameBiome {
        海洋("OCEAN", new ItemBuilderUtil().setType(Material.RAW_FISH).setDurability(2).getItem(), "§7海洋生物群系是基本的", "为所有岛屿启动生物群系", "像动物一样的被动小怪将会", "不会产生。敌对的暴徒会", "正常生成。"),
        森林("FOREST", new ItemBuilderUtil().setType(Material.SAPLING).setDurability(1).getItem(), "森林生物群系将允许", "你的岛屿产生被动。", "像动物一样的怪物（包括", "狼群。敌对小怪将", "正常生成。"),
        沙漠("DESERT", new ItemBuilderUtil().setType(Material.SAND).getItem(), "沙漠生物群系使它如此", "没有下雨或下雪", "在你的岛上。被动的暴徒", "不会产生。敌对的暴徒会", "正常生成。"),
        丛林("JUNGLE", new ItemBuilderUtil().setType(Material.SAPLING).setDurability(3).getItem(), "丛林生物群系很明亮", "和多彩。被动的暴徒", "（包括ocelots）将", "生成。敌对小怪将", "正常生成。"),
        沼泽("SWAMPLAND", new ItemBuilderUtil().setType(Material.WATER_LILY).getItem(), "沼泽生物群系很暗", "而且沉闷。被动的暴徒", "会正常产生并且", "粘液的机会很小", "在晚上生成，取决于", "在月相上。"),
        针叶林("TAIGA", new ItemBuilderUtil().setType(Material.SNOW).getItem(), "针叶林生物群系有雪", "而不是下雨。被动", "小怪会正常产生", "（包括狼）和", "敌对的暴徒会生成。"),
        蘑菇岛("MUSHROOM_ISLAND", new ItemBuilderUtil().setType(Material.RED_MUSHROOM).getItem(), "蘑菇生物群系是", "明亮多彩。", "Mooshrooms是唯一的", "会产生的怪物。", "没有其他被动或", "敌对生物会生成。"),
        地狱("HELL", new ItemBuilderUtil().setType(Material.NETHER_BRICK).getItem(), "地狱生物群系看起来像是", "黑暗而死了。有些", "来自幽冥的暴徒将会", "在这个生物群系中产卵", "（不包括ghasts和"),
        末地("SKY", new ItemBuilderUtil().setType(Material.EYE_OF_ENDER).getItem(), "天空生物群系给你的", "岛上一片特殊的黑暗天空。", "只有末影人会产生", "在这个生物群系中。"),
        平原("PLAINS", new ItemBuilderUtil().setType(Material.LONG_GRASS).setDurability(1).getItem(), "平原生物群落下雨", "而不是雪。被动", "小怪会正常产生", "（包括马）和", "敌对的生物会生成。"),
        极端山丘("EXTREME_HILLS", new ItemBuilderUtil().setType(Material.EMERALD_ORE).getItem(), "极端山丘生物群系。", "被动的怪物会产生", "通常和敌对的", "怪物会产卵。"),
        深海生物("DEEP_OCEAN", new ItemBuilderUtil().setType(Material.PRISMARINE_SHARD).getItem(), "深海生物群系是先进的", "生物群落。像动物一样的被动暴徒会", "没有产生。敌对的怪物", "（包括监护人）将", "正常生成。");

        @Getter
        private final String name;
        @Getter
        private final ItemStack itemStack;
        @Getter
        private final String[] lore;

        GameBiome(String name, ItemStack itemStack, String... lore) {
            this.name = name;
            this.itemStack = itemStack;
            this.lore = lore;
        }

        public static GameBiome getByBiome(Biome biome) {
            for (GameBiome gameBiome : GameBiome.values()) {
                if (gameBiome.getName().equals(biome.toString())) {
                    return gameBiome;
                }
            }

            return null;
        }
    }
}

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
            OWNER("??????"), ADMIN("?????????"), DEFAULT("???");

            @Getter
            private final String name;

            PlayerType(String name) {
                this.name = name;
            }
        }
    }

    public enum GameBiome {
        ??????("OCEAN", new ItemBuilderUtil().setType(Material.RAW_FISH).setDurability(2).getItem(), "??7??????????????????????????????", "?????????????????????????????????", "????????????????????????????????????", "?????????????????????????????????", "???????????????"),
        ??????("FOREST", new ItemBuilderUtil().setType(Material.SAPLING).setDurability(1).getItem(), "???????????????????????????", "???????????????????????????", "?????????????????????????????????", "????????????????????????", "???????????????"),
        ??????("DESERT", new ItemBuilderUtil().setType(Material.SAND).getItem(), "??????????????????????????????", "?????????????????????", "?????????????????????????????????", "?????????????????????????????????", "???????????????"),
        ??????("JUNGLE", new ItemBuilderUtil().setType(Material.SAPLING).setDurability(3).getItem(), "???????????????????????????", "???????????????????????????", "?????????ocelots??????", "????????????????????????", "???????????????"),
        ??????("SWAMPLAND", new ItemBuilderUtil().setType(Material.WATER_LILY).getItem(), "????????????????????????", "??????????????????????????????", "?????????????????????", "?????????????????????", "???????????????????????????", "???????????????"),
        ?????????("TAIGA", new ItemBuilderUtil().setType(Material.SNOW).getItem(), "???????????????????????????", "????????????????????????", "?????????????????????", "??????????????????", "???????????????????????????"),
        ?????????("MUSHROOM_ISLAND", new ItemBuilderUtil().setType(Material.RED_MUSHROOM).getItem(), "?????????????????????", "???????????????", "Mooshrooms????????????", "?????????????????????", "?????????????????????", "????????????????????????"),
        ??????("HELL", new ItemBuilderUtil().setType(Material.NETHER_BRICK).getItem(), "?????????????????????????????????", "????????????????????????", "???????????????????????????", "??????????????????????????????", "????????????ghasts???"),
        ??????("SKY", new ItemBuilderUtil().setType(Material.EYE_OF_ENDER).getItem(), "???????????????????????????", "????????????????????????????????????", "????????????????????????", "???????????????????????????"),
        ??????("PLAINS", new ItemBuilderUtil().setType(Material.LONG_GRASS).setDurability(1).getItem(), "????????????????????????", "?????????????????????", "?????????????????????", "??????????????????", "???????????????????????????"),
        ????????????("EXTREME_HILLS", new ItemBuilderUtil().setType(Material.EMERALD_ORE).getItem(), "???????????????????????????", "????????????????????????", "??????????????????", "??????????????????"),
        ????????????("DEEP_OCEAN", new ItemBuilderUtil().setType(Material.PRISMARINE_SHARD).getItem(), "??????????????????????????????", "????????????????????????????????????????????????", "??????????????????????????????", "????????????????????????", "???????????????");

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

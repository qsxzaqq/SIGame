package cc.i9mc.sigame.data;

import cc.i9mc.gameutils.BukkitGameUtils;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.EnumDifficulty;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JinVan on 2021-01-13.
 */
@Data
public class SIData {
    private static Gson GSON = new Gson();
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static ConcurrentHashMap<UUID, SIData> DATA = new ConcurrentHashMap<>();

    private UUID uuid;
    private String name;
    private SIType type;
    private boolean pvp;
    private BorderColor borderColor;
    private int size;
    private EnumDifficulty difficulty;
    private Biome biome;

    private boolean lock;
    private List<Player> members;
    private List<Player> trusts;

    private Location spawn;
    private Location warp;

    private long lastTime;
    private boolean rain = false;

    public void sendMessage(String message) {
        List<org.bukkit.entity.Player> members = new ArrayList<>(Bukkit.getWorld(getUuid().toString()).getPlayers());
        members.addAll(Bukkit.getWorld(getUuid().toString() + "_nether").getPlayers());

        members.forEach(player -> player.sendMessage(message));
    }

    public void sendMessage(TextComponent message) {
        List<org.bukkit.entity.Player> members = new ArrayList<>(Bukkit.getWorld(getUuid().toString()).getPlayers());
        members.addAll(Bukkit.getWorld(getUuid().toString() + "_nether").getPlayers());

        members.forEach(player -> player.spigot().sendMessage(message));
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
        private UUID uuid;
        private String name;

        private Date joinTime;
        private Date lastTime;

        public boolean isOnline() {
            return Bukkit.getPlayer(uuid) != null;
        }

        public static Player createPlayer(org.bukkit.entity.Player player) {
            Player rawPlayer = new Player();
            rawPlayer.setUuid(player.getUniqueId());
            rawPlayer.setName(player.getName());
            return rawPlayer;
        }
    }

    public boolean hasMember(UUID uuid) {
        for (Player trust : members) {
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

    public static SIData getSIData(UUID uuid) {
        if (!DATA.containsKey(uuid)) {
            SIData siData = Database.loadSIData(uuid);
            if (siData != null) {
                DATA.put(uuid, siData);
            }
        }

        return DATA.get(uuid);
    }

    public void updatePVP(boolean pvp) {
        this.pvp = pvp;

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.pvp = ? WHERE si_data.uuid = ?");
                preparedStatement.setBoolean(1, pvp);
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateBorderColor(BorderColor borderColor) {
        this.borderColor = borderColor;

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.border_color = ? WHERE si_data.uuid = ?");
                preparedStatement.setString(1, borderColor.toString());
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateSize(int size) {
        this.size = size;

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.size = ? WHERE si_data.uuid = ?");
                preparedStatement.setInt(1, size);
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateBiome(Biome biome) {
        this.biome = biome;

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.biome = ? WHERE si_data.uuid = ?");
                preparedStatement.setString(1, this.biome.toString());
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateLock(boolean lock) {
        this.lock = lock;

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET si_data.lock = ? WHERE si_data.uuid = ?");
                preparedStatement.setBoolean(1, lock);
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void saveMember() {
        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET members = ? WHERE uuid = ?");
                preparedStatement.setString(1, GSON.toJson(members));
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void saveTrusts() {
        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET trusts = ? WHERE uuid = ?");
                preparedStatement.setString(1, GSON.toJson(trusts));
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateSpawn(org.bukkit.Location location) {
        this.spawn = Location.createLocation(location);

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET spawn = ? WHERE uuid = ?");
                preparedStatement.setString(1, GSON.toJson(this.spawn));
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public void updateWarp(org.bukkit.Location location) {
        this.warp = Location.createLocation(location);

        executorService.execute(() -> {
            try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_data SET warp = ? WHERE uuid = ?");
                preparedStatement.setString(1, GSON.toJson(this.warp));
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }
}

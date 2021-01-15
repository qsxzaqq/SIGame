package cc.i9mc.sigame.data;

import cc.i9mc.gameutils.BukkitGameUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.v1_12_R1.EnumDifficulty;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by JinVan on 2021-01-14.
 */
public class Database {
    private static final Gson GSON = new Gson();

    public static boolean hasData(UUID uuid) {
        boolean has = false;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_members.uuid AS 'player_uuid', si_members.name AS 'player_name', si_members.joinTime AS 'player_joinTime', si_members.lastTime AS 'player_lastTime', si_data.uuid, si_data.name, si_data.type, si_data.border, si_data.biome, si_data.spawn, si_data.warp FROM si_members, si_data WHERE si_members.data_id = si_data.id AND si_members.uuid = ? AND si_data.lock = false");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                has = true;
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            has = false;
        }

        return has;
    }

    public static boolean hasData(String name) {
        boolean has = false;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_members.uuid AS 'player_uuid', si_members.name AS 'player_name', si_members.joinTime AS 'player_joinTime', si_members.lastTime AS 'player_lastTime', si_data.uuid, si_data.name, si_data.type, si_data.border, si_data.biome, si_data.spawn, si_data.warp FROM si_members, si_data WHERE si_members.data_id = si_data.id AND si_members.name = ? AND si_data.lock = false");
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                has = true;
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            has = false;
        }

        return has;
    }

    public static SIData loadSIData(UUID uuid) {
        SIData siData = new SIData();

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_data.uuid, si_data.name, si_data.type, si_data.pvp, si_data.border_color, si_data.size, si_data.difficulty, si_data.biome, si_data.spawn, si_data.warp FROM si_data WHERE si_data.uuid = ? AND si_data.lock = false");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                siData.setUuid(uuid);
                siData.setName(resultSet.getString("name"));
                siData.setType(SIType.valueOf(resultSet.getString("type")));
                siData.setPvp(resultSet.getBoolean("pvp"));
                siData.setBorderColor(BorderColor.valueOf(resultSet.getString("border_color")));
                siData.setSize(resultSet.getInt("size"));
                siData.setDifficulty(EnumDifficulty.valueOf(resultSet.getString("difficulty")));
                siData.setBiome(Biome.valueOf(resultSet.getString("biome")));
                siData.setLock(false);
                siData.setSpawn(GSON.fromJson(resultSet.getString("spawn"), SIData.Location.class));
                siData.setWarp(GSON.fromJson(resultSet.getString("warp"), SIData.Location.class));
            } else {
                siData = null;
            }
            resultSet.close();

            if (siData != null) {
                siData.setMembers(new ArrayList<>());
                preparedStatement = connection.prepareStatement("SELECT si_members.uuid, si_members.name, si_members.joinTime, si_members.lastTime FROM si_members WHERE si_members.data_id = ?");
                preparedStatement.setString(1, uuid.toString());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getString("uuid").equals(uuid.toString())) {
                        continue;
                    }
                    SIData.Player player = new SIData.Player();
                    player.setUuid(UUID.fromString(resultSet.getString("uuid")));
                    player.setName(resultSet.getString("name"));
                    player.setJoinTime(resultSet.getDate("joinTime"));
                    player.setLastTime(resultSet.getDate("lastTime"));
                    siData.getMembers().add(player);
                }
                resultSet.close();

                siData.setTrusts(new ArrayList<>());
                preparedStatement = connection.prepareStatement("SELECT si_trusts.uuid, si_trusts.name, si_trusts.joinTime, si_trusts.lastTime FROM si_trusts WHERE si_trusts.data_id = ?");
                preparedStatement.setString(1, uuid.toString());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    if (resultSet.getString("uuid").equals(uuid.toString())) {
                        continue;
                    }
                    SIData.Player player = new SIData.Player();
                    player.setUuid(UUID.fromString(resultSet.getString("uuid")));
                    player.setName(resultSet.getString("name"));
                    player.setJoinTime(resultSet.getDate("joinTime"));
                    player.setLastTime(resultSet.getDate("lastTime"));
                    siData.getTrusts().add(player);
                }
                resultSet.close();
            }

            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            siData = null;
        }

        return siData;
    }

    public static boolean hasTrust(UUID uuid, String name) {
        boolean has = false;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_trusts.uuid AS 'player_uuid', si_trusts.name AS 'player_name', si_trusts.joinTime AS 'player_joinTime', si_trusts.lastTime AS 'player_lastTime', si_data.uuid FROM si_trusts, si_data WHERE si_trusts.data_id = si_data.id AND si_data.uuid = ? AND si_trusts.name = ? AND si_data.lock = false;");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                has = true;
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            has = false;
        }

        return has;
    }

    public static void addTrust(UUID uuid, SIData.Player player) {
        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO si_trusts(data_id, uuid, name, joinTime, lastTime) VALUES ((SELECT si_data.id FROM si_data WHERE si_data.uuid = ?), ?, ?, ?, ?)");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setString(2, player.getUuid().toString());
            preparedStatement.setString(3, player.getName());
            preparedStatement.setDate(4, (Date) player.getJoinTime());
            preparedStatement.setDate(5, (Date) player.getLastTime());
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void updateMemberLastTime(UUID uuid) {
        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_members SET si_members.lastTime = ? WHERE si_members.uuid = ?");
            preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void updateTrustLastTime(UUID uuid) {
        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE si_trusts SET si_trusts.lastTime = ? WHERE si_trusts.uuid = ?");
            preparedStatement.setDate(1, new Date(System.currentTimeMillis()));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();

            preparedStatement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}

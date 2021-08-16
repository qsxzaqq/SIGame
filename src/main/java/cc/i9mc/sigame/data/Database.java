package cc.i9mc.sigame.data;

import cc.i9mc.gameutils.BukkitGameUtils;
import com.google.gson.Gson;
import org.bukkit.block.Biome;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JinVan on 2021-01-14.
 */
public class Database {
    public static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static final Gson GSON = new Gson();

    public static boolean has(UUID uuid) {
        boolean has = false;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT uuid FROM si_members WHERE data_id != null AND uuid = ?");
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

    public static boolean has(String name) {
        boolean has = false;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT name FROM si_members WHERE data_id != null AND name = ?");
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

    /***
     * 获取玩家岛屿
     * @param uuid member的UUID
     * @return SIData
     */
    public static SIData load(UUID uuid) {
        SIData siData = new SIData();

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_data.id, si_data.type, si_data.pvp, si_data.border_color, si_data.size, si_data.open, si_data.biome, si_data.lock, si_data.spawn, si_data.warp FROM si_data, si_members WHERE si_members.uuid = ? AND si_data.id = si_members.data_id AND si_data.lock = false");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                siData.setId(resultSet.getInt("id"));
                siData.setType(SIData.GameType.valueOf(resultSet.getString("type")));
                siData.setPvp(resultSet.getBoolean("pvp"));
                siData.setBorderColor(SIData.BorderColor.valueOf(resultSet.getString("border_color")));
                siData.setSize(resultSet.getInt("size"));
                siData.setOpen(resultSet.getBoolean("open"));
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
                preparedStatement = connection.prepareStatement("SELECT * FROM si_members WHERE data_id = ?");
                preparedStatement.setInt(1, siData.getId());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    SIData.Player player = new SIData.Player();
                    player.setId(resultSet.getInt("id"));
                    player.setUuid(UUID.fromString(resultSet.getString("uuid")));
                    player.setName(resultSet.getString("name"));
                    player.setJoinTime(resultSet.getDate("joinTime"));
                    player.setLastTime(resultSet.getDate("lastTime"));
                    player.setPlayerType(SIData.Player.PlayerType.valueOf(resultSet.getString("type")));

                    if (player.getPlayerType() == SIData.Player.PlayerType.OWNER) {
                        siData.setOwner(player);
                        continue;
                    }
                    siData.getMembers().add(player);
                }
                resultSet.close();

                siData.setTrusts(new ArrayList<>());
                preparedStatement = connection.prepareStatement("SELECT si_trusts.id,si_trusts.uuid, si_trusts.name, si_trusts.joinTime, si_trusts.lastTime FROM si_trusts WHERE si_trusts.data_id = ?");
                preparedStatement.setInt(1, siData.getId());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    SIData.Player player = new SIData.Player();
                    player.setId(resultSet.getInt("id"));
                    if (resultSet.getString("uuid") != null) {
                        player.setUuid(UUID.fromString(resultSet.getString("uuid")));
                    }
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

    public static SIData loadByData(UUID uuid) {
        SIData siData = new SIData();

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_data.id, si_data.type, si_data.pvp, si_data.border_color, si_data.size, si_data.open, si_data.biome, si_data.lock, si_data.spawn, si_data.warp FROM si_data, si_members WHERE si_members.uuid = ? AND si_data.id = si_members.data_id");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                siData.setId(resultSet.getInt("id"));
                siData.setType(SIData.GameType.valueOf(resultSet.getString("type")));
                siData.setPvp(resultSet.getBoolean("pvp"));
                siData.setBorderColor(SIData.BorderColor.valueOf(resultSet.getString("border_color")));
                siData.setSize(resultSet.getInt("size"));
                siData.setOpen(resultSet.getBoolean("open"));
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
                preparedStatement = connection.prepareStatement("SELECT * FROM si_members WHERE data_id = ?");
                preparedStatement.setInt(1, siData.getId());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    SIData.Player player = new SIData.Player();
                    player.setId(resultSet.getInt("id"));
                    player.setUuid(UUID.fromString(resultSet.getString("uuid")));
                    player.setName(resultSet.getString("name"));
                    player.setJoinTime(resultSet.getDate("joinTime"));
                    player.setLastTime(resultSet.getDate("lastTime"));
                    player.setPlayerType(SIData.Player.PlayerType.valueOf(resultSet.getString("type")));

                    if (player.getPlayerType() == SIData.Player.PlayerType.OWNER) {
                        siData.setOwner(player);
                        continue;
                    }
                    siData.getMembers().add(player);
                }
                resultSet.close();

                siData.setTrusts(new ArrayList<>());
                preparedStatement = connection.prepareStatement("SELECT si_trusts.id,si_trusts.uuid, si_trusts.name, si_trusts.joinTime, si_trusts.lastTime FROM si_trusts WHERE si_trusts.data_id = ?");
                preparedStatement.setInt(1, siData.getId());
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    SIData.Player player = new SIData.Player();
                    player.setId(resultSet.getInt("id"));
                    if (resultSet.getString("uuid") != null) {
                        player.setUuid(UUID.fromString(resultSet.getString("uuid")));
                    }
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

/*
    public static boolean hasTrust(UUID uuid, String name) {
        boolean has = false;

        try (Connection connection = BukkitGameUtils.getInstance().getConnectionPoolHandler().getConnection("sidata")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT si_trusts.uuid AS 'player_uuid', si_trusts.name AS 'player_name', si_trusts.joinTime AS 'player_joinTime', si_trusts.lastTime AS 'player_lastTime', si_data.uuid FROM si_trusts, si_data WHERE si_trusts.data_id = si_data.id AND si_data.uuid = ? AND si_trusts.name = ?;");
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
    }*/

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

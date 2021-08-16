package cc.i9mc.sigame.data;

import cc.i9mc.gameutils.utils.board.Board;
import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.tasks.GameScoreTask;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JinVan on 2021-01-16.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GamePlayer extends SIData.Player {
    public static final ConcurrentHashMap<UUID, GamePlayer> DATA = new ConcurrentHashMap<>();

    private Board board;
    private UUID islandUuid;
    private Double money;
    private Integer points;

    public static void init(GamePlayer gamePlayer) {
        DATA.put(gamePlayer.getUuid(), gamePlayer);
    }

    public static void load(Player player) {
        GamePlayer gamePlayer = get(player.getUniqueId());
        gamePlayer.setName(player.getName());
        gamePlayer.setBoard(new Board(player, "SB", Collections.singletonList("Test")));
        GameScoreTask.setShow(player);
    }

    public static GamePlayer get(UUID uuid) {
        return DATA.getOrDefault(uuid, null);
    }

    public void updateMoneyAndPoints() {
        points = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI().look(getUuid());
        money = SIGame.getInstance().getEcon().getBalance(getPlayer());
    }
}

package cc.i9mc.sigame.tasks;

import cc.i9mc.gameutils.utils.board.Board;
import cc.i9mc.sigame.data.GamePlayer;
import cc.i9mc.sigame.data.SIData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by JinVan on 2021-01-16.
 */
public class GameScoreTask implements Runnable {
    private static final Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
    private static GameScoreTask instance;
    private static Objective hp;
    private static Objective o;
    private final List<String> groups = Arrays.asList("§f官方QQ群①: 537913186", "§f官方QQ群②: 870707720", "§f官方QQ群③: 340349295");
    private int time = 1;
    private int group = 0;

    public GameScoreTask() {
        instance = this;
    }

    public static void setShow(Player player) {
        player.setScoreboard(sb);
        instance.run();
    }

    @Override
    public void run() {
        if (time > 6) {
            group++;
            time = 0;
        }

        if (group > 2) {
            group = 0;
        }

        if (hp == null) {
            hp = sb.registerNewObjective("NAME_HEALTH", "health");
            hp.setDisplaySlot(DisplaySlot.BELOW_NAME);
            hp.setDisplayName(ChatColor.RED + "❤");
        }
        if (o == null) {
            o = sb.registerNewObjective("health", "dummy");
            o.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        for (GamePlayer gamePlayer : GamePlayer.DATA.values()) {
            Board board = gamePlayer.getBoard();
            Player player = gamePlayer.getPlayer();
            if (player == null) {
                continue;
            }

            if ((gamePlayer.getMoney() == null || gamePlayer.getPoints() == null) || time == 5) {
                gamePlayer.updateMoneyAndPoints();
            }

            String title;
            switch (time) {
                case 1:
                    title = "§e§l游";
                    break;
                case 2:
                    title = "§e§l游戏";
                    break;
                case 3:
                    title = "§e§l游戏世";
                    break;
                default:
                    title = "§e§l游戏世界";
                    break;
            }

            String group1 = groups.get(group);

            SIData siData = SIData.getByWorld(player.getWorld());

            hp.getScore(player.getName()).setScore((int) player.getHealth());
            o.getScore(player.getName()).setScore((int) player.getHealth());


            List<String> list = new ArrayList<>();
            list.add("§e+§7--------------------");
            list.add(" §e!  §7名称: §f" + player.getName());
            list.add(" §e!  §7点券: §f" + gamePlayer.getPoints());
            list.add(" §e!  §7金币: §f" + gamePlayer.getMoney());
            list.add(" §e!  §7位置: §f" + (siData != null ? siData.getOwner().getName() + "的岛屿" : "none"));
            list.add("§e+§7--------------------");
            list.add("  " + group1);
            list.add("§e+§7--------------------");
            list.add("§b          mcyc.win");

            board.send(title, list);
        }

        time++;
    }
}

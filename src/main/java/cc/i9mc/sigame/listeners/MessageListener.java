package cc.i9mc.sigame.listeners;

import cc.i9mc.gameutils.GameUtilsAPI;
import cc.i9mc.gameutils.event.bukkit.BukkitPubSubMessageEvent;
import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.MemberRequest;
import cc.i9mc.sigame.data.MemberResult;
import cc.i9mc.sigame.data.SIGameJoin;
import cc.i9mc.sigame.event.MemberAcceptEvent;
import cc.i9mc.sigame.event.MemberDenyEvent;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by JinVan on 2021-01-12.
 */
public class MessageListener implements Listener {
    private static final Gson GSON = new Gson();

    @EventHandler
    public void onSub(BukkitPubSubMessageEvent event) {
        switch (event.getChannel()) {
            case "SI.Game.Join":
                SIGameJoin siGameJoin = GSON.fromJson(event.getMessage(), SIGameJoin.class);
                if (!siGameJoin.getServer().equals(GameUtilsAPI.getServerName())) {
                    return;
                }

                SIGame.getInstance().getPlayerManager().addData(siGameJoin);
                break;
            case "SI.Game.MemberRequest":
                MemberRequest memberRequest = GSON.fromJson(event.getMessage(), MemberRequest.class);
                break;
            case "SI.Game.MemberResult":
                MemberResult memberResult = GSON.fromJson(event.getMessage(), MemberResult.class);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (memberResult.getMemberResultType() == MemberResult.MemberResultType.ACCEPT) {
                            Bukkit.getPluginManager().callEvent(new MemberAcceptEvent(memberResult));
                        } else {
                            Bukkit.getPluginManager().callEvent(new MemberDenyEvent(memberResult));
                        }
                    }
                }.runTask(SIGame.getInstance());
                break;
        }
    }
}

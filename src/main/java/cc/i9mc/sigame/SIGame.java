package cc.i9mc.sigame;

import cc.i9mc.gameutils.BukkitGameUtils;
import cc.i9mc.k8sgameack.K8SGameACK;
import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.listeners.MessageListener;
import cc.i9mc.sigame.listeners.PlayerListener;
import cc.i9mc.sigame.listeners.WorldListener;
import cc.i9mc.sigame.manager.AcidEffectManager;
import cc.i9mc.sigame.manager.PlayerManager;
import cc.i9mc.sigame.manager.WorldManager;
import cc.i9mc.sigame.tasks.UnloadWorldTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by JinVan on 2021-01-12.
 */
public class SIGame extends JavaPlugin {
    @Getter
    private static SIGame instance;
    @Getter
    private WorldManager worldManager;
    @Getter
    private PlayerManager playerManager;
    @Getter
    private AcidEffectManager acidEffectManager;

    private BukkitTask bukkitTask;

    @Override
    public void onEnable() {
        instance = this;

        BukkitGameUtils.getInstance().getConnectionPoolHandler().registerDatabase("sidata");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.Join");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.MemberRequest");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.MemberResult");

        K8SGameACK.getInstance().getServerData().setGameType("SI");
        K8SGameACK.getInstance().getExpand().put("version", getDescription().getVersion());

        worldManager = new WorldManager();
        playerManager = new PlayerManager();
        acidEffectManager = new AcidEffectManager();

        Bukkit.getPluginManager().registerEvents(new MessageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);

        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new UnloadWorldTask(), 0L, 20L);
    }

    @Override
    public void onDisable() {
        bukkitTask.cancel();
        for (Player player : getServer().getOnlinePlayers()) {
            player.kickPlayer("服务器维护");
        }

        Iterator<Map.Entry<UUID, SIData>> iterator = SIData.DATA.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SIData> entry = iterator.next();

            SIGame.getInstance().getWorldManager().unloadWorld(entry.getKey());
            entry.getValue().updateLock(false);
            iterator.remove();
        }
    }
}

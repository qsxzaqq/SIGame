package cc.i9mc.sigame;

import cc.i9mc.gameutils.BukkitGameUtils;
import cc.i9mc.gameutils.event.bukkit.BukkitSendNameEvent;
import cc.i9mc.k8sgameack.K8SGameACK;
import cc.i9mc.k8sgameack.data.ServerType;
import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.listeners.MessageListener;
import cc.i9mc.sigame.listeners.PlayerListener;
import cc.i9mc.sigame.listeners.WorldListener;
import cc.i9mc.sigame.manager.AcidEffectManager;
import cc.i9mc.sigame.manager.PlayerManager;
import cc.i9mc.sigame.manager.WorldManager;
import cc.i9mc.sigame.tasks.GameScoreTask;
import cc.i9mc.sigame.tasks.UnloadWorldTask;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;
import java.util.Map;

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
    @Getter
    private Economy econ = null;
    @Getter
    private Chat chat = null;

    private BukkitTask bukkitTask;


    @Override
    public void onEnable() {
        instance = this;

        BukkitGameUtils.getInstance().getConnectionPoolHandler().registerDatabase("sidata");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.Update");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.Join");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.MemberRequest");
        BukkitGameUtils.getInstance().getPubSubListener().addChannel("SI.Game.MemberResult");

        K8SGameACK.getInstance().getServerData().setGameType("SIGame");
        K8SGameACK.getInstance().getExpand().put("version", getDescription().getVersion());
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onName(BukkitSendNameEvent event) {
                K8SGameACK.getInstance().getServerData().setServerType(ServerType.WAITING);
                K8SGameACK.getInstance().getServerData().setMaxPlayers(Bukkit.getMaxPlayers());
            }
        }, this);

        worldManager = new WorldManager();
        playerManager = new PlayerManager();
        acidEffectManager = new AcidEffectManager();

        Bukkit.getPluginManager().registerEvents(new MessageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);

        setupChat();
        setupEconomy();

        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new UnloadWorldTask(), 0L, 20L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new GameScoreTask(), 20L, 20L);
    }

    @Override
    public void onDisable() {
        bukkitTask.cancel();
        for (Player player : getServer().getOnlinePlayers()) {
            player.kickPlayer("???????????????");
        }

        Iterator<Map.Entry<Integer, SIData>> iterator = SIData.DATA.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, SIData> entry = iterator.next();

            SIGame.getInstance().getWorldManager().unloadWorld(entry.getValue());
            entry.getValue().updateLock(false);
            iterator.remove();
        }
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
    }
}

package cc.i9mc.sigame.tasks;

import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.SIData;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JinVan on 2021-01-14.
 */
public class UnloadWorldTask implements Runnable {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public void run() {
        Iterator<Map.Entry<UUID, SIData>> iterator = SIData.DATA.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SIData> entry = iterator.next();

            if (entry.getValue().getLastTime() != 0L && Math.abs(System.currentTimeMillis() - entry.getValue().getLastTime()) >= 300000L) {
                executorService.execute(() -> {
                    SIGame.getInstance().getWorldManager().unloadWorld(entry.getKey());
                    entry.getValue().updateLock(false);
                    iterator.remove();
                });
                continue;
            }

            if (Bukkit.getWorld(entry.getKey().toString()) == null) {
                continue;
            }

            if (Bukkit.getWorld(entry.getKey().toString()).getPlayers().size() == 0 && Bukkit.getWorld(entry.getKey().toString() + "_nether").getPlayers().size() == 0) {
                if (entry.getValue().getLastTime() == 0L) {
                    entry.getValue().setLastTime(System.currentTimeMillis());
                }
            } else {
                entry.getValue().setLastTime(0L);
            }
        }
    }
}

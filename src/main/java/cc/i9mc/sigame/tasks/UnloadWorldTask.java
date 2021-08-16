package cc.i9mc.sigame.tasks;

import cc.i9mc.sigame.SIGame;
import cc.i9mc.sigame.data.Database;
import cc.i9mc.sigame.data.SIData;
import org.bukkit.Bukkit;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by JinVan on 2021-01-14.
 */
public class UnloadWorldTask implements Runnable {


    @Override
    public void run() {
        Iterator<Map.Entry<Integer, SIData>> iterator = SIData.DATA.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, SIData> entry = iterator.next();

            if (entry.getValue().getLastTime() != 0L && Math.abs(System.currentTimeMillis() - entry.getValue().getLastTime()) >= 300000L) {
                Database.executorService.execute(() -> {
                    SIGame.getInstance().getWorldManager().unloadWorld(entry.getValue());
                    entry.getValue().updateLock(false);
                    iterator.remove();
                });
                continue;
            }

            if (entry.getValue().isLoadWorld()) {
                continue;
            }

            if (entry.getValue().getOnlinePlayers().size() == 0) {
                if (entry.getValue().getLastTime() == 0L) {
                    entry.getValue().setLastTime(System.currentTimeMillis());
                }
            } else {
                entry.getValue().setLastTime(0L);
            }
        }
    }
}

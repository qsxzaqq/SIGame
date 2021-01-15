package cc.i9mc.sigame.tasks;

import cc.i9mc.sigame.data.SIData;
import org.bukkit.Bukkit;

/**
 * Created by JinVan on 2021-01-14.
 */
public class SetBiomeTask implements Runnable {

    private final SIData siData;

    public SetBiomeTask(SIData siData) {
        this.siData = siData;
    }

    @Override
    public void run() {
        for (int x = (int) -siData.getSize(); x < siData.getSize(); x++) {
            for (int z = (int) -siData.getSize(); z < siData.getSize(); z++) {
                Bukkit.getWorld(siData.getUuid()).setBiome(x, z, siData.getBiome());
            }
        }
    }
}

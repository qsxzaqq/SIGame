package cc.i9mc.sigame.manager;

import cc.i9mc.sigame.data.SIData;
import cc.i9mc.sigame.generator.GameGenerator;
import cc.i9mc.sigame.generator.SkyChunkGenerator;
import cc.i9mc.sigame.generator.WaterChunkGenerator;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.File;

/**
 * Created by JinVan on 2021-01-12.
 */
public class WorldManager {
    private final MinecraftServer minecraftServer = MinecraftServer.getServer();
    private final File dataFile = new File("/island/");

    public void loadPlayer(SIData siData) {
        loadWorld(siData.getId().toString(), siData.getId().toString(), World.Environment.NORMAL, getGenerator(siData.getType()), EnumDifficulty.HARD);
        loadWorld(siData.getId().toString(), siData.getId().toString() + "_nether", World.Environment.NETHER, getGenerator(siData.getType()), EnumDifficulty.HARD);
    }

    public void loadWorld(String fileName, String name, World.Environment environment, GameGenerator gameGenerator, EnumDifficulty enumDifficulty) {
        IDataManager serverNBTManager = new ServerNBTManager(getDataFile(environment), fileName, true, ((CraftServer) Bukkit.getServer()).getHandle().getServer().dataConverterManager);
        WorldData worldData = serverNBTManager.getWorldData();
        worldData.a(name);
        WorldServer worldServer = (WorldServer) new WorldServer(minecraftServer, serverNBTManager, worldData, getDimension(), minecraftServer.methodProfiler, environment, gameGenerator).b();

        worldServer.tracker = new EntityTracker(worldServer);
        worldServer.addIWorldAccess(new net.minecraft.server.v1_12_R1.WorldManager(minecraftServer, worldServer));
        worldServer.worldData.setDifficulty(enumDifficulty);
        worldServer.setSpawnFlags(true, true);
        minecraftServer.worlds.add(worldServer);

        Bukkit.getPluginManager().callEvent(new WorldInitEvent(worldServer.getWorld()));
        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(worldServer.getWorld()));
    }

    public void unloadWorld(SIData siData) {
        Bukkit.unloadWorld(siData.getId().toString(), true);
        Bukkit.unloadWorld(siData.getId().toString() + "_nether", true);
    }

    private int getDimension() {
        int dimension = 10 + minecraftServer.worlds.size();
        boolean used = false;

        do {
            for (WorldServer server : minecraftServer.worlds) {
                used = server.dimension == dimension;
                if (used) {
                    ++dimension;
                    break;
                }
            }
        } while (used);

        return dimension;
    }

    private File getDataFile(World.Environment environment) {
        if (environment == World.Environment.NORMAL) {
            return new File(dataFile, "world");
        } else if (environment == World.Environment.NETHER) {
            return new File(dataFile, "nether");
        } else {
            return null;
        }
    }

    private GameGenerator getGenerator(SIData.GameType gameType) {
        if (gameType == SIData.GameType.SKY) {
            return new SkyChunkGenerator();
        } else if (gameType == SIData.GameType.WATER) {
            return new WaterChunkGenerator();
        } else {
            return null;
        }
    }
}

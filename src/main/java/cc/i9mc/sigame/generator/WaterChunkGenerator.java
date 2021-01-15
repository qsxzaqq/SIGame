package cc.i9mc.sigame.generator;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

/**
 * Created by JinVan on 2021-01-13.
 */
public class WaterChunkGenerator extends GameGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        setBiome(world, biome);

        ChunkData chunkData = createChunkData(world);
        chunkData.setRegion(0, 0, 0, 16, 50, 16, Material.WATER);

        return chunkData;
    }

}

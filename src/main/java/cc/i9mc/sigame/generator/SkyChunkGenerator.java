package cc.i9mc.sigame.generator;

import org.bukkit.World;

import java.util.Random;

/**
 * Created by JinVan on 2021-01-13.
 */
public class SkyChunkGenerator extends GameGenerator {

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        setBiome(world, biome);

        return createChunkData(world);
    }

}

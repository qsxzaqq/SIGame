package cc.i9mc.sigame.generator;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Collections;
import java.util.List;

/**
 * Created by JinVan on 2021-01-13.
 */
public class GameGenerator extends ChunkGenerator {

    public void setBiome(World world, BiomeGrid biomeGrid) {
        Biome biome = Biome.OCEAN;
        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                biomeGrid.setBiome(x, z, biome);
            }
        }
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        return Collections.emptyList();
    }
}

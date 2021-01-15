package cc.i9mc.sigame.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.noise.PerlinOctaveGenerator;

import java.util.Random;

/**
 * Created by JinVan on 2021-01-13.
 */
public class NetherChunkGenerator extends GameGenerator {
    private final Random rand = new Random();

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        setBiome(world, biome);

        ChunkData chunkData = createChunkData(world);
        PerlinOctaveGenerator perlinOctaveGenerator = new PerlinOctaveGenerator((long) (rand.nextLong() * rand.nextGaussian()), 8);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, world.getMaxHeight() - 1, z, Material.BEDROCK);
                for (int y = 2; y < 5; y++) {
                    double r = perlinOctaveGenerator.noise(x, world.getMaxHeight() - y, z, 0.5, 0.5);
                    if (r > 0D) {
                        chunkData.setBlock(x, world.getMaxHeight() - y, z, Material.BEDROCK);
                    }
                }

                for (int y = 5; y < 8; y++) {
                    double r = perlinOctaveGenerator.noise(x, world.getMaxHeight() - y, z, 0.5, 0.5);
                    if (r > 0D) {
                        chunkData.setBlock(x, world.getMaxHeight() - y, z, Material.NETHERRACK);
                    } else {
                        chunkData.setBlock(x, world.getMaxHeight() - y, z, Material.AIR);
                    }
                }

                double r = perlinOctaveGenerator.noise(x, world.getMaxHeight() - 8, z, rand.nextFloat(), rand.nextFloat());
                if (r > 0.5D) {
                    switch (rand.nextInt(4)) {
                        case 1:
                            chunkData.setBlock(x, world.getMaxHeight() - 8, z, Material.GLOWSTONE);
                            if (x < 14 && z < 14) {
                                chunkData.setBlock(x + 1, world.getMaxHeight() - 8, z + 1, Material.GLOWSTONE);
                                chunkData.setBlock(x + 2, world.getMaxHeight() - 8, z + 2, Material.GLOWSTONE);
                                chunkData.setBlock(x + 1, world.getMaxHeight() - 8, z + 2, Material.GLOWSTONE);
                                chunkData.setBlock(x + 1, world.getMaxHeight() - 8, z + 2, Material.GLOWSTONE);
                            }
                            break;
                        case 2:
                            for (int i = 0; i < rand.nextInt(10); i++) {
                                chunkData.setBlock(x, world.getMaxHeight() - 8 - i, z, Material.GLOWSTONE);
                            }
                            break;
                        case 3:
                            chunkData.setBlock(x, -8, z, Material.GLOWSTONE);
                            if (x > 3 && z > 3) {
                                for (int xx = 0; xx < 3; xx++) {
                                    for (int zz = 0; zz < 3; zz++) {
                                        chunkData.setBlock(x - xx, world.getMaxHeight() - 8 - rand.nextInt(2), z - xx, Material.GLOWSTONE);
                                    }
                                }
                            }
                            break;
                        default:
                            chunkData.setBlock(x, world.getMaxHeight() - 8, z, Material.GLOWSTONE);
                    }
                    chunkData.setBlock(x, world.getMaxHeight() - 8, z, Material.GLOWSTONE);
                } else {
                    chunkData.setBlock(x, world.getMaxHeight() - 8, z, Material.AIR);
                }
            }
        }

        return chunkData;
    }
}

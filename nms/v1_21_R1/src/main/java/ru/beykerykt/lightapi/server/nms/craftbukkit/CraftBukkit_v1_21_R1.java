/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2017 The ImplexDevOne Project
 * Copyright (c) 2019 Vladimir Mikhailov <beykerykt@gmail.com>
 * Copyright (c) 2021 LOOHP <jamesloohp@gmail.com>
 * Copyright (c) 2021 Qveshn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.beykerykt.lightapi.server.nms.craftbukkit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;
import ru.beykerykt.lightapi.server.nms.NmsHandlerBase;

/**
 * Paper 1.21.1+ implementation using public Paper API
 * No NMS reflection required - uses direct Paper APIs
 */
public class CraftBukkit_v1_21_R1 extends NmsHandlerBase {

	public CraftBukkit_v1_21_R1() {
		// No initialization needed - using public APIs only
	}

	@Override
	public void createLight(World world, int x, int y, int z, LightType lightType, int light) {
		// Paper provides direct API for light manipulation
		// Validate light level
		int lightLevel = light < 0 ? 0 : Math.min(light, 15);
		
		Location location = new Location(world, x, y, z);
		
		if (lightType == LightType.SKY) {
			world.setLightLevel(location, (byte) lightLevel, true);
		} else {
			world.setLightLevel(location, (byte) lightLevel, false);
		}
		
		// Recalculate after setting
		recalculateLighting(world, x, y, z, lightType);
	}

	@Override
	public void deleteLight(World world, int x, int y, int z, LightType lightType) {
		// Delete light by setting it to 0
		Location location = new Location(world, x, y, z);
		
		if (lightType == LightType.SKY) {
			world.setLightLevel(location, (byte) 0, true);
		} else {
			world.setLightLevel(location, (byte) 0, false);
		}
		
		recalculateLighting(world, x, y, z, lightType);
	}

	@Override
	protected void recalculateLighting(World world, int blockX, int blockY, int blockZ, LightType lightType) {
		// Paper handles light recalculation automatically when using setLightLevel
		// No manual recalculation needed with Paper's APIs
		
		// Optional: Update chunk if needed
		int chunkX = blockX >> 4;
		int chunkZ = blockZ >> 4;
		
		// Ensure chunk is loaded
		if (world.isChunkLoaded(chunkX, chunkZ)) {
			world.getChunkAt(chunkX, chunkZ).getChunkSnapshot(false, true, false);
		}
	}

	@Override
	public void sendChunkSectionsUpdate(
			World world, int chunkX, int chunkZ, 
			BitSet sectionsMaskSky, BitSet sectionsMaskBlock, Player player
	) {
		// Paper handles chunk updates automatically
		// When light is changed, Paper updates affected sections automatically
		// This method is a no-op for Paper API
	}

	@Override
	public List<ChunkInfo> collectChunks(
			World world, int blockX, int blockY, int blockZ, LightType lightType, int lightLevel
	) {
		if (lightType != LightType.SKY || lightLevel < 15) {
			return super.collectChunks(world, blockX, blockY, blockZ, lightType, lightLevel);
		}
		
		List<ChunkInfo> list = new ArrayList<>();
		Collection<Player> players = null;
		
		for (int dx = -1; dx <= 1; dx++) {
			int lightLevelX = lightLevel - getDeltaLight(blockX & 15, dx);
			if (lightLevelX > 0) {
				for (int dz = -1; dz <= 1; dz++) {
					int lightLevelZ = lightLevelX - getDeltaLight(blockZ & 15, dz);
					if (lightLevelZ > 0) {
						if (lightLevelZ > getDeltaLight(blockY & 15, 1)) {
							int sectionY = (blockY >> 4) + 1;
							if (isValidSectionY(world, sectionY)) {
								int chunkX = blockX >> 4;
								int chunkZ = blockZ >> 4;
								ChunkInfo cCoord = new ChunkInfo(
										world,
										chunkX + dx,
										sectionY << 4,
										chunkZ + dz,
										players != null ? players : (players = world.getPlayers()));
								list.add(cCoord);
							}
						}
						
						for (int sectionY = blockY >> 4; isValidSectionY(world, sectionY); sectionY--) {
							if (isValidSectionY(world, sectionY)) {
								int chunkX = blockX >> 4;
								int chunkZ = blockZ >> 4;
								ChunkInfo cCoord = new ChunkInfo(
										world,
										chunkX + dx,
										sectionY << 4,
										chunkZ + dz,
										players != null ? players : (players = world.getPlayers()));
								list.add(cCoord);
							}
						}
					}
				}
			}
		}
		
		return list;
	}

	@Override
	public boolean isSupported(World world, LightType lightType) {
		// Paper API supports both sky and block light for all worlds
		return world != null;
	}

	@Override
	public int getMinLightHeight(World world) {
		return world.getMinHeight() - 16;
	}

	@Override
	public int getMaxLightHeight(World world) {
		return world.getMaxHeight() + 16;
	}

	@Override
	public BitSet asSectionMask(World world, int sectionY) {
		BitSet bitset = new BitSet();
		bitset.set((sectionY - (world.getMinHeight() >> 4)) + 1, true);
		return bitset;
	}

	@Override
	protected int getViewDistance(Player player) {
		return player.getClientViewDistance();
	}

	private int getDeltaLight(int x, int dx) {
		return (((x ^ ((-dx >> 4) & 15)) + 1) & (-(dx & 1)));
	}
}

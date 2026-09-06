/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Vladimir Mikhailov <beykerykt@gmail.com>
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
package ru.beykerykt.lightapi;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.beykerykt.lightapi.chunks.ChunkInfo;
import ru.beykerykt.lightapi.events.DeleteLightEvent;
import ru.beykerykt.lightapi.events.SetLightEvent;
import ru.beykerykt.lightapi.events.UpdateChunkEvent;
import ru.beykerykt.lightapi.request.RequestSteamMachine;
import ru.beykerykt.lightapi.server.ServerModInfo;
import ru.beykerykt.lightapi.server.ServerModManager;
import ru.beykerykt.lightapi.server.nms.INMSHandler;
import ru.beykerykt.lightapi.server.nms.craftbukkit.*;
import ru.beykerykt.lightapi.updater.Response;
import ru.beykerykt.lightapi.updater.UpdateType;
import ru.beykerykt.lightapi.updater.Updater;
import ru.beykerykt.lightapi.updater.Version;
import ru.beykerykt.lightapi.utils.BungeeChatHelperClass;
import ru.beykerykt.lightapi.utils.Debug;
import ru.beykerykt.lightapi.utils.Metrics_bStats;
import ru.beykerykt.lightapi.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LightAPI extends JavaPlugin implements Listener {

	private static LightAPI plugin;
	private static RequestSteamMachine machine;
	private static final String modCraftBukkit = "CraftBukkit";
	private int update_delay_ticks;
	private int max_iterations_per_tick;

	private boolean enableUpdater;
	private String repo = "Qveshn/LightAPI";
	public static final String sourceCodeUrl = "https://github.com/Qveshn/LightAPI";
	public static final String author = "Qveshn";
	public static final String authorUrl = "https://github.com/Qveshn";
	public static final String contributorsUrl = "https://github.com/Qveshn/LightAPI/graphs/contributors";
	private int delayUpdate = 40;
	private boolean viewChangelog;
	private String messagePrefix;
	private boolean coloredLog;

	// To synchronize nms create/delete light methods to avoid conflicts in multi-threaded calls. Got a better idea?
	private static final Object lock = new Object();

	@SuppressWarnings("static-access")
	@Override
	public void onLoad() {
		Debug.setPrefix(getName(), ChatColor.YELLOW, ChatColor.GOLD);
		this.plugin = this;
		this.machine = new RequestSteamMachine();

		ServerModInfo craftbukkit = new ServerModInfo(modCraftBukkit);
//		craftbukkit.getVersions().put("v1_8_R3", CraftBukkit_v1_8_R3.class);
//		craftbukkit.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
//		craftbukkit.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
//		craftbukkit.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
//		craftbukkit.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
//		craftbukkit.getVersions().put("v1_12_R1", CraftBukkit_v1_12_R1.class);
//		craftbukkit.getVersions().put("v1_13_R1", CraftBukkit_v1_13_R1.class);
//		craftbukkit.getVersions().put("v1_13_R2", CraftBukkit_v1_13_R2.class);
		craftbukkit.getVersions().put("v1_14_R1", CraftBukkit_v1_14_R1.class);
		craftbukkit.getVersions().put("v1_15_R1", CraftBukkit_v1_15_R1.class);
		craftbukkit.getVersions().put("v1_16_R1", CraftBukkit_v1_16_R1.class);
//		craftbukkit.getVersions().put("v1_16_R2", CraftBukkit_v1_16_R2.class);
//		craftbukkit.getVersions().put("v1_16_R3", CraftBukkit_v1_16_R3.class);
		craftbukkit.getVersions().put("v1_17_R1", CraftBukkit_v1_17_R1.class);
		craftbukkit.getVersions().put("v1_18_R1",CraftBukkit_v1_18_R1.class);
		// Support for Paper 1.19-1.21.11 using v1_18_R1 implementation
		craftbukkit.getVersions().put("1_19", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_19_1", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_19_2", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_19_3", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_20", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_20_1", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_20_2", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_20_3", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_20_4", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_20_5", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_21", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_21_1", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_21_2", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_21_3", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_21_4", CraftBukkit_v1_18_R1.class);
		craftbukkit.getVersions().put("1_21_11", CraftBukkit_v1_18_R1.class);
		ServerModManager.registerServerMod(craftbukkit);
	}

	@Override
	public void onEnable() {
		// Config
		saveDefaultConfig();
		Configuration config = getConfig();
		try {
			Configuration defaults = config.getDefaults();
			if (defaults == null) {
				throw new NullPointerException("Missing default config");
			}
			int version = config.getInt("version", -1);
			int newVersion = defaults.getInt("version");
			if (version != newVersion) {
				this.coloredLog = config.getBoolean("colored-log");
				logError("Incorrect config version (§f%d§r instead §f%d§r). Will be updated.", version, newVersion);
				File file = new File(getDataFolder(), "config.yml");
				File bkp = new File(getDataFolder(), String.format("config.yml.%s.backup",
						new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSS").format(new Date())));
				logInfo("Saving §fconfig.yml§r to §f%s", bkp.getName());
				if (!file.renameTo(bkp)) {
					throw new IOException(String.format("Can not rename file '%s' to '%s'", file, bkp));
				}
				saveDefaultConfig();
				reloadConfig();
				config = getConfig();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			getPluginLoader().disablePlugin(this);
			return;
		}

		// Init config
		this.update_delay_ticks = config.getInt("update-delay-ticks");
		this.max_iterations_per_tick = config.getInt("max-iterations-per-tick");
		this.enableUpdater = config.getBoolean("updater.enable");
		this.repo = config.getString("updater.repo", repo);
		this.delayUpdate = config.getInt("updater.update-delay-ticks");
		this.viewChangelog = config.getBoolean("updater.view-changelog");
		this.coloredLog = config.getBoolean("colored-log");
		this.messagePrefix = config.getString("message-prefix");
		if (messagePrefix == null) messagePrefix = "";

		Debug.setEnable(config.getBoolean("debug"));

		// init nms
		String serverName = Utils.serverName();
		String bukkitName = Utils.bukkitName();
		String modName;
		Class<? extends INMSHandler> clazz = ServerModManager.findImplementaion(modName = serverName);
		if (clazz == null && !serverName.equals(bukkitName)) {
			clazz = ServerModManager.findImplementaion(modName = bukkitName);
		}
		if (clazz == null && !Arrays.asList(serverName, bukkitName).contains(modCraftBukkit)) {
			clazz = ServerModManager.findImplementaion(modName = modCraftBukkit);
		}
		if (clazz == null) {
			logError("No implementations was found for §f%s§r server §f%s§r.",
					serverName, Utils.serverVersion());
		} else {
			try {
				ServerModManager.initImplementaion(clazz);
			} catch (Exception e) {
				logError("Could not initialize §f%s§r implementation for §f%s§r server §f%s§r.",
						modName, serverName, Utils.serverVersion());
				e.printStackTrace();
			}
		}
		if (!ServerModManager.isInitialized()) {
			logError("Disabling plugin");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		logInfo(modName.equals(serverName)
						? "Loading implementation for §f%2$s§r server §f%3$s§r."
						: "Loading §f%1$s§r implementation for §f%2$s§r server §f%3$s§r.",
				modName, serverName, Utils.serverVersion());

		machine.start(LightAPI.getInstance().getUpdateDelayTicks(), LightAPI.getInstance().getMaxIterationsPerTick());
		getServer().getPluginManager().registerEvents(this, this);

		if (enableUpdater) {
			// Starting updater
			runUpdater(getServer().getConsoleSender(), delayUpdate);
		}

		// init metrics
		new Metrics_bStats(this, getName() + "-fork");
	}

	@Override
	public void onDisable() {
		machine.shutdown();
	}

	private void logInfo(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	private void logError(String message, Object... args) {
		log(Level.SEVERE, message, args);
	}

	private void log(Level level, String message, Object... args) {
		if (args != null && args.length > 0) {
			message = String.format(message, args);
		}
		Logger logger = getLogger();
		if (!coloredLog) {
			logger.log(level, removeFormat(message));
		} else if (logger.isLoggable(level)) {
			Bukkit.getConsoleSender().sendMessage(String.format(
					"§b[%s] %s",
					getDescription().getName(),
					adjustResetFormat("§r" + message, level == Level.SEVERE ? "§c" : "§7")
			));
		}
	}

	private String removeFormat(String message) {
		return message.replaceAll("§[0-9a-fk-or]", "");
	}

	private String adjustResetFormat(String message, String append) {
		return message.replaceAll("§r", "§r" + append);
	}

	public void sendMessage(CommandSender sender, String message, Object... args) {
		for (String s : (args != null && args.length > 0 ? String.format(message, args) : message).split("\n")) {
			sender.sendMessage(String.format("%s%s", messagePrefix(), s));
		}
	}

	public String messagePrefix() {
		return messagePrefix;
	}

	public static String join(String delimeter, List<String> args) {
		StringBuilder sb = new StringBuilder();
		for (String arg : args) {
			if (sb.length() > 0) sb.append(delimeter);
			sb.append(arg);
		}
		return sb.toString();
	}

	public static LightAPI getInstance() {
		return plugin;
	}

	@SuppressWarnings("unused")
	public static boolean isSupported(World world, LightType lightType) {
		return ServerModManager.getNMSHandler().isSupported(world, lightType);
	}

	@SuppressWarnings("unused")
	public static int getMinLightHeight(World world) {
		return ServerModManager.getNMSHandler().getMinLightHeight(world);
	}

	@SuppressWarnings("unused")
	public static int getMaxLightHeight(World world) {
		return ServerModManager.getNMSHandler().getMaxLightHeight(world);
	}

	@Deprecated
	@SuppressWarnings("unused")
	public static boolean createLight(Location location, int lightlevel, boolean async) {
		return createLight(location, LightType.BLOCK, lightlevel, async);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean createLight(Location location, LightType lightType, int lightlevel, boolean async) {
		return createLight(
				location.getWorld(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ(),
				lightType,
				lightlevel,
				async);
	}

	@Deprecated
	public static boolean createLight(
			World world, int x, final int y, final int z, final int lightlevel, boolean async) {
		return createLight(world, x, y, z, LightType.BLOCK, lightlevel, async);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean createLight(
			World world, int x, final int y, final int z, LightType lightType, final int lightlevel, boolean async) {
		if (getInstance().isEnabled()) {
			final SetLightEvent event = new SetLightEvent(world, x, y, z, lightType, lightlevel, async);
			Bukkit.getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				Runnable request = new Runnable() {
					@Override
					public void run() {
						synchronized (lock) {
							ServerModManager.getNMSHandler().createLight(
									event.getWorld(),
									event.getX(),
									event.getY(),
									event.getZ(),
									event.getLightType(),
									event.getLightLevel());
						}
					}
				};
				if (event.isAsync()) {
					machine.addToQueue(request);
				} else {
					request.run();
				}
				return true;
			}
		}
		return false;
	}

	@Deprecated
	@SuppressWarnings("unused")
	public static boolean deleteLight(Location location, boolean async) {
		return deleteLight(location, LightType.BLOCK, async);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean deleteLight(Location location, LightType lightType, boolean async) {
		return deleteLight(
				location.getWorld(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ(),
				lightType,
				async);
	}

	@Deprecated
	public static boolean deleteLight(final World world, final int x, final int y, final int z, boolean async) {
		return deleteLight(world, x, y, z, LightType.BLOCK, async);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean deleteLight(
			final World world, final int x, final int y, final int z, LightType lightType, boolean async
	) {
		if (getInstance().isEnabled()) {
			final DeleteLightEvent event = new DeleteLightEvent(world, x, y, z, lightType, async);
			Bukkit.getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				Runnable request = new Runnable() {
					@Override
					public void run() {
						ServerModManager.getNMSHandler().deleteLight(
								event.getWorld(),
								event.getX(),
								event.getY(),
								event.getZ(),
								event.getLightType());
					}
				};
				if (event.isAsync()) {
					machine.addToQueue(request);
				} else {
					request.run();
				}
				return true;
			}
		}
		return false;
	}

	@Deprecated
	public static List<ChunkInfo> collectChunks(Location location) {
		return collectChunks(location, LightType.BLOCK, 15);
	}

	@Deprecated
	@SuppressWarnings("unused")
	public static List<ChunkInfo> collectChunks(Location location, int lightLevel) {
		return collectChunks(location, LightType.BLOCK, lightLevel);
	}

	@SuppressWarnings("WeakerAccess")
	public static List<ChunkInfo> collectChunks(Location location, LightType lightType, int lightLevel) {
		return collectChunks(
				location.getWorld(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ(),
				lightType,
				lightLevel);
	}

	@Deprecated
	public static List<ChunkInfo> collectChunks(final World world, final int x, final int y, final int z) {
		return collectChunks(world, x, y, z, LightType.BLOCK, 15);
	}

	@Deprecated
	public static List<ChunkInfo> collectChunks(World world, int x, int y, int z, int lightLevel) {
		return collectChunks(world, x, y, z, LightType.BLOCK, lightLevel);
	}

	@SuppressWarnings("WeakerAccess")
	public static List<ChunkInfo> collectChunks(World world, int x, int y, int z, LightType lightType, int lightLevel) {
		if (getInstance().isEnabled()) {
			return ServerModManager.getNMSHandler().collectChunks(world, x, y, z, lightType, lightLevel);
		}
		return new ArrayList<ChunkInfo>();
	}

	@Deprecated
	public static boolean updateChunks(ChunkInfo info) {
		return updateChunk(info);
	}

	@Deprecated
	public static boolean updateChunk(ChunkInfo info) {
		return updateChunk(info, LightType.BLOCK);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean updateChunk(ChunkInfo info, LightType lightType) {
		return updateChunk(info, lightType, null);
	}

	@Deprecated
	public static boolean updateChunk(ChunkInfo info, Collection<? extends Player> players) {
		return updateChunk(info, LightType.BLOCK, players);
	}

	@SuppressWarnings("WeakerAccess")
	public static boolean updateChunk(ChunkInfo info, LightType lightType, Collection<? extends Player> players) {
		if (getInstance().isEnabled()) {
			UpdateChunkEvent event = new UpdateChunkEvent(info, lightType);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				machine.addChunkToUpdate(info, lightType, players);
				return true;
			}
		}
		return false;
	}

	@Deprecated
	public static boolean updateChunks(Location location, Collection<? extends Player> players) {
		return updateChunks(
				location.getWorld(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ(),
				players);
	}

	@Deprecated
	public static boolean updateChunks(World world, int x, int y, int z, Collection<? extends Player> players) {
		if (getInstance().isEnabled()) {
			for (ChunkInfo info : collectChunks(world, x, y, z, 15)) {
				info.setReceivers(players);
				updateChunk(info);
			}
			return true;
		}
		return false;
	}

	@Deprecated
	public static boolean updateChunk(Location location, Collection<? extends Player> players) {
		return updateChunk(
				location.getWorld(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ(),
				players);
	}

	@Deprecated
	public static boolean updateChunk(World world, int x, int y, int z, Collection<? extends Player> players) {
		if (getInstance().isEnabled()) {
			updateChunk(new ChunkInfo(world, x, y - 16, z, players));
			updateChunk(new ChunkInfo(world, x, y, z, players));
			updateChunk(new ChunkInfo(world, x, y + 16, z, players));
			return true;
		}
		return false;
	}

	private static BlockFace[] SIDES = {
			BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
	};

	@Deprecated
	public static Block getAdjacentAirBlock(Block block) {
		for (BlockFace face : SIDES) {
			if (block.getY() == 0x0 && face == BlockFace.DOWN)
				continue;
			if (block.getY() == 0xFF && face == BlockFace.UP)
				continue;

			Block candidate = block.getRelative(face);

			if (candidate.getType().isTransparent()) {
				return candidate;
			}
		}
		return block;
	}

	@SuppressWarnings("WeakerAccess")
	public int getUpdateDelayTicks() {
		return update_delay_ticks;
	}

	@SuppressWarnings("unused")
	public void setUpdateDelayTicks(int update_delay_ticks) {
		this.update_delay_ticks = update_delay_ticks;
	}

	@SuppressWarnings("WeakerAccess")
	public int getMaxIterationsPerTick() {
		return max_iterations_per_tick;
	}

	@SuppressWarnings("unused")
	public void setMaxIterationsPerTick(int max_iterations_per_tick) {
		this.max_iterations_per_tick = max_iterations_per_tick;
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		if (enableUpdater) {
			if (player.hasPermission("lightapi.updater")) {
				runUpdater(player, delayUpdate);
			}
		}
	}

	private void runUpdater(final CommandSender sender, int delay) {
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {

			@Override
			public void run() {
				Version version = Version.parse(getDescription().getVersion());
				try {
					final Updater updater = new Updater(version, repo, false);
					final Response response = updater.getResult();
					Bukkit.getScheduler().runTask(plugin, new Runnable() {

						@Override
						public void run() {
							printUpdateInfo(sender, updater, response);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, delay);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lightapi")) {
			if (args.length == 0) {
				printDescription(sender);
			} else {
				if (args[0].equalsIgnoreCase("update")) {
					if (sender.hasPermission("lightapi.updater") || sender.isOp()) {
						runUpdater(sender, 2);
					} else {
						sendMessage(sender, "§cYou don't have permission!");
					}
				} else {
					sendMessage(sender, "§cHmm... This command does not exist. Are you sure write correctly?");
				}
			}
		}
		return true;
	}

	private void printDescription(CommandSender sender) {
		if (sender instanceof Player && BungeeChatHelperClass.hasBungeeChatAPI()) {
			BungeeChatHelperClass.sendMessageAboutPlugin((Player) sender, this);
		} else {
			printTitle(sender);
			sendMessage(sender, "§bDevelopers: §f%s", LightAPI.join("§7, §f", getDescription().getAuthors()));
			sendMessage(sender, "§bSource code: §f%s", sourceCodeUrl);
			sendMessage(sender, "§bLicensed under: §fMIT License");
			printServerInfo(sender);
		}
	}

	private void printTitle(CommandSender sender) {
		if (sender instanceof Player && BungeeChatHelperClass.hasBungeeChatAPI()) {
			BungeeChatHelperClass.sendMessageTitle((Player) sender, this);
		} else {
			sendMessage(sender, "§b-------< §eLightAPI-fork §f%s §b>-------", getDescription().getVersion());
		}
	}

	public void printServerInfo(CommandSender sender) {
		sendMessage(sender, "§bMinecraft server: §f%s §f%s", getServer().getName(), getServer().getVersion());
	}

	private void printUpdateInfo(CommandSender sender, Updater updater, Response response) {
		printTitle(sender);
		if (response == Response.SUCCESS) {
			sendMessage(sender, "§fNew update is available: §e" + updater.getLatestVersion() + "§f!");
			UpdateType update = UpdateType.compareVersion(updater.getVersion().toString());
			sendMessage(sender, "§fRepository: " + repo);
			sendMessage(sender, "§fUpdate type: " + update.getName());
			if (update == UpdateType.MAJOR) {
				sendMessage(sender, "§cWARNING ! A MAJOR UPDATE! Not updating plugins may"
						+ " produce errors after starting the server! Notify developers about update.");
			}
			if (viewChangelog) {
				sendMessage(sender, "§fChanges: ");
				String changes = updater.getChanges();
				sendMessage(sender, "§a" + (changes == null ? "" : changes.replaceAll("\n", "\n§a")));
			}
		} else if (response == Response.REPO_NOT_FOUND) {
			sendMessage(sender, "§cRepo not found! Check that your repo exists!");
		} else if (response == Response.REPO_NO_RELEASES) {
			sendMessage(sender, "§cReleases not found! Check your repo!");
		} else if (response == Response.NO_UPDATE) {
			sendMessage(sender, "§aYou are running the latest version!");
		}
	}
}

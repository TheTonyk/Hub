package com.thetonyk.Hub.Managers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.github.paperspigot.Title;

import com.thetonyk.Hub.Main;
import com.thetonyk.Hub.Managers.PermissionsManager.Rank;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;

public class PlayersManager implements Listener {
	
	public static Set<UUID> cooldown = new HashSet<>();
	
	public static String getField(UUID uuid, String field) throws SQLException {
		
		try (Connection connection = DatabaseManager.getConnection();
		Statement statement = connection.createStatement();
		ResultSet query = statement.executeQuery("SELECT " + field + " FROM users WHERE uuid = '" + uuid.toString() + "';")) {
			
			if (!query.next()) return null;
				
			return query.getString(field);
			
		}
		
	}
	
	public static Rank getRank(UUID uuid) throws SQLException {
		
		return Rank.valueOf(getField(uuid, "rank"));
		
	}
	
	public static void updateNametag(Player player) throws SQLException {
		
		String name = player.getName();
		Rank rank = getRank(player.getUniqueId());
		
		for (Player online : Bukkit.getOnlinePlayers()) {
			
			String onlineName = online.getName();
			Rank onlineRank = getRank(online.getUniqueId());
			Scoreboard scoreboard = online.getScoreboard();
			Team team = scoreboard.getTeam(name);
			
			if (team == null) {
				
				team = scoreboard.registerNewTeam(name);
				team.setAllowFriendlyFire(true);
				team.setCanSeeFriendlyInvisibles(false);
				team.setNameTagVisibility(NameTagVisibility.ALWAYS);
				
			}
			
			team.setPrefix(rank.getPrefix() + "§7");
			team.setSuffix("§7");
			team.addEntry(name);
			
			scoreboard = player.getScoreboard();
			team = scoreboard.getTeam(onlineName);
			
			if (team == null) {
				
				team = scoreboard.registerNewTeam(onlineName);
				team.setAllowFriendlyFire(true);
				team.setCanSeeFriendlyInvisibles(false);
				team.setNameTagVisibility(NameTagVisibility.ALWAYS);
				
			}
			
			team.setPrefix(onlineRank.getPrefix() + "§7");
			team.setSuffix("§7");
			team.addEntry(onlineName);
			
		}
		
	}
	
	public static void hideCoords(Player player) {
		
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(nmsPlayer, (byte) 22);
		
		nmsPlayer.playerConnection.sendPacket(packet);
		
	}
	
	public static void showCoords(Player player) {
		
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(nmsPlayer, (byte) 23);
		
		nmsPlayer.playerConnection.sendPacket(packet);
		
	}
	
	public static void sendActionBar(Player player, String message) {
		
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		IChatBaseComponent jsonText = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
		PacketPlayOutChat packet = new PacketPlayOutChat(jsonText, (byte) 2);
		
		nmsPlayer.playerConnection.sendPacket(packet);
		
	}
	
	public static void clearPlayer(Player player) {
		
		PlayerInventory inventory = player.getInventory();
		InventoryView openedInventory = player.getOpenInventory();
		
		inventory.clear();
		inventory.setArmorContents(null);
		player.setItemOnCursor(new ItemStack(Material.AIR));
		
		if (openedInventory.getType() == InventoryType.CRAFTING) openedInventory.getTopInventory().clear();
		
		player.setLevel(0);
		player.setTotalExperience(0);
		player.setExp(0f);
		player.setFoodLevel(20);
		player.setSaturation(5f);
		player.setExhaustion(0f);
		player.setHealth(player.getMaxHealth());
		player.getActivePotionEffects().stream().forEach(e -> player.removePotionEffect(e.getType()));
		
	}
	
	public static void updatePlayers(Player player) throws SQLException {
		
		updatePlayers(player, Settings.getSettings(player.getUniqueId()));
		
	}
	
	public static void updatePlayers(Player player, Settings settings) throws SQLException {
		
		for (Player online : Bukkit.getOnlinePlayers()) {
			
			if (online.equals(player)) continue;
			
			Settings onlineSettings = Settings.getSettings(online.getUniqueId());
			
			if (!settings.getPlayers() && !online.hasPermission("global.visible")) player.hidePlayer(online);
			
			if (!onlineSettings.getPlayers() && !player.hasPermission("global.visible")) online.hidePlayer(player);
			
		}
		
	}
	
	public static void sendToServer(Player player, String server) throws IOException {
		
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		
		try (DataOutputStream output = new DataOutputStream(array)) {
			
			output.writeUTF("Connect");
			output.writeUTF(server);
			
		}
		
		player.sendPluginMessage(Main.plugin, "BungeeCord", array.toByteArray());
		
	}	
	
	private static void error(PlayerLoginEvent event) {
		
		event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§8⫸ §7An error has occured while connecting to §a" + Main.NAME + " §8⫷\n\n§7Please try again later or contact us on Twitter §b" + Main.TWITTER);
		
	}
	
	private static void error(PlayerJoinEvent event) {
		
		event.setJoinMessage(null);
		event.getPlayer().kickPlayer("§8⫸ §7An error has occured while connecting to §a" + Main.NAME + " §8⫷\n\n§7Please try again later or contact us on Twitter §b" + Main.TWITTER);
		
	}

	@EventHandler
	public void onConnect(PlayerLoginEvent event) {
		
		Player player = event.getPlayer();
		
		try {
			
			PermissionsManager.setPermissions(player);
			
		} catch (SQLException exception) {
			
			error(event);
			return;
			
		}
		
		if (event.getResult() != PlayerLoginEvent.Result.KICK_WHITELIST) return;
			
		if (player.isOp() || player.hasPermission("global.bypasswhitelist")) event.allow();
		else event.setKickMessage("§8⫸ §7You are not whitelisted §8⫷");
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		
		try {
			
			if (player.isDead()) player.spigot().respawn();
			
			Title title = new Title("§9" + Main.NAME, "§7Welcome on the Hub §7⋯ §a" + (Main.count > 0 ? Main.count : Bukkit.getOnlinePlayers().size()) + " §7players", 0, 60, 10);
			
			player.sendTitle(title);
			event.setJoinMessage(null);
			player.teleport(player.getWorld().getSpawnLocation());
			player.setGameMode(GameMode.ADVENTURE);
			player.spigot().setViewDistance(8);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			updateNametag(player);
			hideCoords(player);
			clearPlayer(player);
			
			if (player.hasPermission("global.fly")) player.setAllowFlight(true);
			
			updatePlayers(player);
			
		} catch (SQLException exception) {
			
			error(event);
			return;
			
		}
		
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
			
		event.setQuitMessage(null);
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		showCoords(player);
		
		if (player.isInsideVehicle()) player.leaveVehicle();
		
		for (Player online : Bukkit.getOnlinePlayers()) {
			
			Scoreboard scoreboard = online.getScoreboard();
			Team team = scoreboard.getTeam(player.getName());
			
			if (team != null) team.unregister();
			
		}
		
		PermissionsManager.clearPermissions(player);
		
	}

}

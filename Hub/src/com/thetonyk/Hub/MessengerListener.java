package com.thetonyk.Hub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.thetonyk.Hub.Managers.PermissionsManager;
import com.thetonyk.Hub.Managers.PlayersManager;

public class MessengerListener implements PluginMessageListener {

	public synchronized void onPluginMessageReceived(String channel, Player sender, byte[] message) {
		
		if (channel.equals(Main.CHANNEL)) {
		
			try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
				
				String subchannel = input.readUTF();
				
				if (subchannel.equals("updateRank")) {
					
					UUID uuid = UUID.fromString(input.readUTF());
					Player player = Bukkit.getPlayer(uuid);
					
					if (player == null) return;
					
					PlayersManager.updateNametag(player);
					PermissionsManager.reloadPermissions(player);
					return;
					
				}
				
			} catch (IOException | SQLException exception) {}
			
		}
		
		if (channel.equals("BungeeCord")) {
			
			try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(message))) {
				
				String subchannel = input.readUTF();
				
				if (subchannel.equals("GetServers")) {
					
					Main.servers = input.readUTF().split(", ");
					return;
					
				}
				
				if (subchannel.equals("PlayerCount")) {
					
					String server = input.readUTF();
					int count = input.readInt();
					
					if (!server.equalsIgnoreCase("ALL")) return;
					
					Main.count = count;
					return;
					
				}
				
			} catch (IOException exception) {
				
				Player player = Bukkit.getOnlinePlayers().iterator().next();
				
				if (player == null) return;
				
				getServers(player);
				
			}
			
		}
		
	}
	
	private static void getServers(Player player) {
		
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		
		try (DataOutputStream output = new DataOutputStream(array)) {
			
			output.writeUTF("GetServers");
			
		} catch (IOException exception) {}
		
		player.sendPluginMessage(Main.plugin, "BungeeCord", array.toByteArray());
		
	}
	
	private static void getPlayersCount(Player player) {
			
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		
		try (DataOutputStream output = new DataOutputStream(array)) {
			
			output.writeUTF("PlayerCount");
			output.writeUTF("ALL");
			
		} catch (IOException exception) {}
		
		player.sendPluginMessage(Main.plugin, "BungeeCord", array.toByteArray());
		
	}
	
	public static void setup() {
		
		new BukkitRunnable() {
			
			public void run() {
				
				if (Bukkit.getOnlinePlayers().size() < 1) return;
				
				Player player = Bukkit.getOnlinePlayers().iterator().next();
				
				getPlayersCount(player);
				
				if (Main.servers != null && Main.servers.length > 0) return;
				
				getServers(player);
				
			}
			
		}.runTaskTimer(Main.plugin, 10, 10);
		
	}

}

package com.thetonyk.Hub.Features;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import com.thetonyk.Hub.Main;
import com.thetonyk.Hub.Inventories.SettingsInventory;
import com.thetonyk.Hub.Managers.PlayersManager;
import com.thetonyk.Hub.Utils.ItemsUtils;

public class ItemsFeature implements Listener {
	
	private static Map<String, String> servers = new HashMap<>();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		
		ItemStack item = ItemsUtils.createItem(Material.REDSTONE_COMPARATOR, "§8⫸ §6§lSettings §8(§7Right-Click to open§8) §8⫷", 1);
		item = ItemsUtils.hideFlags(item);
		player.getInventory().setItem(8, item);
		
		if (Main.servers == null) return;
		
		servers = getServers();
		String[] list = servers.keySet().toArray(new String[0]);
		
		for (int i = 0; i < list.length; i++) {
			
			item = ItemsUtils.createItem(Material.DIAMOND_SWORD, "§8⫸ §a§l" + list[i] + " §8(§7Right-Click to join§8) §8⫷", 1);
			item = ItemsUtils.setGlowing(item);
			item = ItemsUtils.hideFlags(item);
			player.getInventory().setItem(i, item);
			
		}
		
	}
	
	private static Map<String, String> getServers() {
		
		Map<String, String> servers = new HashMap<>();
		
		for (int i = 0; i < Main.servers.length; i++) {
			
			String server = Main.servers[i];
			
			if (server.equalsIgnoreCase("lobby")) continue;
			
			String name = "";
			
			for (String word : server.split("-")) {
				
				name += word.substring(0, 1).toUpperCase() + word.substring(1, word.length()).toLowerCase();
				
			}
			
			servers.put(name, server);
			
		}
		
		return servers;
		
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		if (!(event.getWhoClicked() instanceof Player)) return;
		
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
	
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
		
		String name = item.getItemMeta().getDisplayName();
		
		if (name.startsWith("§8⫸ §a§l")) {
			
			String server = name.substring(8, name.length() - 32);
			
			try {
				
				PlayersManager.sendToServer(player, servers.get(server));
				
			} catch (IOException exception) {
				
				player.sendMessage(Main.PREFIX + "An error has occured while sending you to the server.");
				return;
				
			}
			
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
			return;
			
		} else if (name.startsWith("§8⫸ §6§l")) {
			
			SettingsInventory inventory = SettingsInventory.getInventory(player);
			player.openInventory(inventory.getInventory());
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
			return;
			
		}
		
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		Action action = event.getAction();
		
		if (action == Action.PHYSICAL) return;
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
		
		String name = item.getItemMeta().getDisplayName();
		
		if (name.startsWith("§8⫸ §a§l")) {
			
			if (Main.serversCooldown.contains(player.getUniqueId())) return;
			
			Main.serversCooldown.add(player.getUniqueId());
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> Main.serversCooldown.remove(player.getUniqueId()), 60);
			
			String server = name.substring(8, name.length() - 32);
			
			try {
				
				PlayersManager.sendToServer(player, servers.get(server));
				
			} catch (IOException exception) {
				
				player.sendMessage(Main.PREFIX + "An error has occured while sending you to the server.");
				return;
				
			}
			
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
			return;
			
		} else if (name.startsWith("§8⫸ §6§l")) {
			
			SettingsInventory inventory = SettingsInventory.getInventory(player);
			player.openInventory(inventory.getInventory());
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
			return;
			
		}
		
	}

}

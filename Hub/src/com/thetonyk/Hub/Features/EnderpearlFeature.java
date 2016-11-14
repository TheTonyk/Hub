package com.thetonyk.Hub.Features;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import com.thetonyk.Hub.Main;
import com.thetonyk.Hub.Managers.PlayersManager;
import com.thetonyk.Hub.Utils.ItemsUtils;

public class EnderpearlFeature implements Listener {
	
	private static Set<UUID> cooldown = new HashSet<>();
	//private static List<Item> pearls = new ArrayList<>();
	
	public static void setup() {
		
		new BukkitRunnable() {

			public void run() {
				
				/*Iterator<Item> iterator = pearls.iterator();
				
				while (iterator.hasNext()) {
					
					Item pearl = iterator.next();
					Vector velocity = pearl.getVelocity().normalize();
					
					if (velocity.getY() != -0.0 && pearl.getPassenger() != null && !pearl.isDead()) continue;
					
					Entity passenger = pearl.getPassenger();
					
					iterator.remove();
					passenger.teleport(passenger.getLocation().add(0, 0.5, 0));
					pearl.remove();
					
				}*/
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					
					if (player.getVehicle() == null || player.getVehicle().getType() != EntityType.ENDER_PEARL) continue;
				
					player.spigot().playEffect(player.getLocation(), Effect.INSTANT_SPELL, 14, 0, 0f, 0f, 0f, 0.15f, 10, 1);
					
				}
				
			}
			
		}.runTaskTimer(Main.plugin, 1, 1);
		
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		
		ItemStack item = ItemsUtils.createItem(Material.EYE_OF_ENDER, "§8⫸ §d§lFlying Pearl §8(§7Right-Click to launch§8) §8⫷", 1);
		item = ItemsUtils.hideFlags(item);
		player.getInventory().setItem(6, item);
		
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		ItemStack item = event.getItem();
		Action action = event.getAction();
		
		if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
		
		String name = item.getItemMeta().getDisplayName();
		
		if (!name.startsWith("§8⫸ §d§l")) return;
		
		event.setCancelled(true);
			
		if (cooldown.contains(player.getUniqueId()) || player.getVehicle() != null) return;
		
		cooldown.add(player.getUniqueId());
		
		new BukkitRunnable() {
			
			int left = 5;

			public void run() {
				
				if (left < 1) {
					
					PlayersManager.sendActionBar(player, "§7You can now use your §d§lFlying Pearl");
					cooldown.remove(player.getUniqueId());
					cancel();
					return;
					
				}
				
				PlayersManager.sendActionBar(player, "§7You can use your §d§lFlying Pearl §7in §a" + left + "§7s");
				left--;
				
			}
			
		}.runTaskTimer(Main.plugin, 0, 20);
		
		/*Item pearl = player.getWorld().dropItem(player.getLocation().add(0, 1, 0), new ItemStack(Material.ENDER_PEARL));
		pearls.add(pearl);
		pearl.setPickupDelay(12000);
		pearl.setVelocity(player.getLocation().getDirection().normalize().multiply(2f));
		pearl.setPassenger(player);*/
		EnderPearl pearl = player.launchProjectile(EnderPearl.class);
		pearl.setPassenger(player);
		player.spigot().setCollidesWithEntities(false);
		player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
		
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		
		Player player = event.getPlayer();
		
		if (event.getCause() != TeleportCause.ENDER_PEARL) return;
		
		event.setCancelled(true);
		player.teleport(player.getLocation().add(0, 0.5, 0));
		player.spigot().setCollidesWithEntities(true);
		
	}
	
	@EventHandler
	public void onDismount(EntityDismountEvent event) {
		
		if (!(event.getDismounted() instanceof EnderPearl)) return;
		if (!(event.getEntity() instanceof Player)) return;
		
		EnderPearl pearl = (EnderPearl) event.getDismounted();
		Player player = (Player) event.getEntity();
		
		pearl.remove();
		//player.teleport(player.getLocation().add(0, 0.5, 0));
		player.spigot().setCollidesWithEntities(true);
		
	}
	
	@EventHandler
	public void onHit(ProjectileHitEvent event) {
		
		if (!(event.getEntity() instanceof EnderPearl)) return;
		
		EnderPearl pearl = (EnderPearl) event.getEntity();
		
		if (!(pearl.getShooter() instanceof Player)) return;
		
		Player player = (Player) pearl.getShooter();
		
		pearl.remove();
		player.teleport(player.getLocation().add(0, 0.5, 0));
		player.spigot().setCollidesWithEntities(true);
		
	}

}

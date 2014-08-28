package com.comze_instancelabs.mgwarlocktactical;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.ArenaSetup;
import com.comze_instancelabs.minigamesapi.ArenaState;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.PluginInstance;
import com.comze_instancelabs.minigamesapi.commands.CommandHandler;
import com.comze_instancelabs.minigamesapi.config.ArenasConfig;
import com.comze_instancelabs.minigamesapi.config.DefaultConfig;
import com.comze_instancelabs.minigamesapi.config.MessagesConfig;
import com.comze_instancelabs.minigamesapi.config.StatsConfig;
import com.comze_instancelabs.minigamesapi.guns.Gun;
import com.comze_instancelabs.minigamesapi.guns.Guns;
import com.comze_instancelabs.minigamesapi.util.Util;
import com.comze_instancelabs.minigamesapi.util.Validator;

public class Main extends JavaPlugin implements Listener {

	// allow custom arenas

	MinigamesAPI api = null;
	PluginInstance pli = null;
	static Main m = null;
	static int global_arenas_size = 30;

	HashMap<String, String> lastdamager = new HashMap<String, String>();
	HashMap<String, Gun> pwait = new HashMap<String, Gun>();

	Guns g = null;

	public void onEnable() {
		m = this;
		api = MinigamesAPI.getAPI().setupAPI(this, "warlocktactic", IArena.class, new ArenasConfig(this), new MessagesConfig(this), new IClassesConfig(this), new StatsConfig(this, false), new DefaultConfig(this, false), true);
		PluginInstance pinstance = api.pinstances.get(this);
		pinstance.addLoadedArenas(loadArenas(this, pinstance.getArenasConfig()));
		Bukkit.getPluginManager().registerEvents(this, this);
		pinstance.arenaSetup = new IArenaSetup();
		pinstance.setRewardsInstance(new IRewards(this));

		IArenaListener t = new IArenaListener(this, pinstance);
		api.registerArenaListenerLater(this, t);
		pinstance.setArenaListener(t);

		pli = pinstance;
		getConfig().addDefault("config.global_arenas_size", 30);
		getConfig().options().copyDefaults(true);
		this.saveConfig();
		global_arenas_size = getConfig().getInt("config.global_arenas_size");

		g = new Guns(this);

		g.loadGuns(this);
	}

	public static ArrayList<Arena> loadArenas(JavaPlugin plugin, ArenasConfig cf) {
		ArrayList<Arena> ret = new ArrayList<Arena>();
		FileConfiguration config = cf.getConfig();
		if (!config.isSet("arenas")) {
			return ret;
		}
		for (String arena : config.getConfigurationSection("arenas.").getKeys(false)) {
			if (Validator.isArenaValid(plugin, arena, cf.getConfig())) {
				ret.add(initArena(arena));
			}
		}
		return ret;
	}

	public static IArena initArena(String arena) {
		IArena a = new IArena(m, arena);
		ArenaSetup s = MinigamesAPI.getAPI().pinstances.get(m).arenaSetup;
		a.init(Util.getSignLocationFromArena(m, arena), Util.getAllSpawns(m, arena), Util.getMainLobby(m), Util.getComponentForArena(m, arena, "lobby"), s.getPlayerCount(m, arena, true), s.getPlayerCount(m, arena, false), s.getArenaVIP(m, arena));
		a.setRadius(global_arenas_size);
		return a;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		CommandHandler ch = new CommandHandler();
		ch.handleArgs(this, "mgwarlocktactic", "/" + cmd.getName(), sender, args);
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("upgrades")) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					g.openGUI(p.getName());
				}
			}
		}
		return true;
	}

	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDrop(final PlayerDropItemEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			if (event.getItemDrop().getItemStack().getType() == Material.FIREBALL) {
				Bukkit.getScheduler().runTaskLater(m, new Runnable() {
					public void run() {
						Location l = event.getItemDrop().getLocation();
						l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 3.5F, false, false);
						event.getItemDrop().remove();
					}
				}, 60L);
			} else {
				event.setCancelled(true);
			}
		}
	}

	private HashMap<String, Integer> pusage = new HashMap<String, Integer>();

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player p = event.getPlayer();
		if (pli.global_players.containsKey(p.getName())) {
			if (event.hasItem()) {
				if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					final ItemStack item = event.getItem();
					HashMap<Gun, int[]> t_ = this.evaluateGun(event.getItem(), p);
					Gun g_ = (Gun) t_.keySet().toArray()[0];
					int[] t = t_.get(g_);
					if (g_ != null) {
						if (pwait.containsKey(p.getName())) {
							if (pwait.get(p.getName()) == g_) {
								p.sendMessage(ChatColor.RED + "Please wait until the gun is reloaded again.");
								return;
							}
						}
						g_.shoot(p, t[2], t[1], t[0]);
					}
				}

				if (pli.global_players.get(p.getName()).getArenaState() != ArenaState.INGAME) {
					if (event.getItem().getType() == Material.DIAMOND_AXE) {
						g.openGUI(p.getName());
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onProjectileLand(ProjectileHitEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {
			if (e.getEntity() instanceof Snowball) {
				Player player = (Player) e.getEntity().getShooter();
				if (pli.global_players.containsKey(player.getName())) {
					BlockIterator bi = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);
					Block hit = null;
					while (bi.hasNext()) {
						hit = bi.next();
						if (hit.getTypeId() != 0) {
							break;
						}
					}
					try {
						if (Math.abs(hit.getLocation().getBlockY() - pli.global_players.get(player.getName()).getSpawns().get(0).getBlockY()) < 3 && hit.getType() == Material.ICE) {
							hit.setTypeId(0);
							hit.getWorld().createExplosion(hit.getLocation(), 1F);

							player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);

						}
					} catch (Exception ex) {

					}
				}
			}
		}
	}

	public HashMap<Gun, int[]> evaluateGun(final ItemStack item, final Player p) {
		HashMap<Gun, int[]> ret = new HashMap<Gun, int[]>();
		Gun g_ = null;
		int[] t = new int[4];
		if (item.getType() == Material.IRON_AXE) {
			g_ = pli.getAllGuns().get("freeze");
			if (g.pgunattributes.containsKey(p.getName())) {
				t = g.pgunattributes.get(p.getName()).containsKey(g_) ? g.pgunattributes.get(p.getName()).get(g_) : g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			} else {
				t = g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			}
		} else if (item.getType() == Material.IRON_HOE) {
			g_ = pli.getAllGuns().get("sniper");
			if (g.pgunattributes.containsKey(p.getName())) {
				t = g.pgunattributes.get(p.getName()).containsKey(g_) ? g.pgunattributes.get(p.getName()).get(g_) : g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			} else {
				t = g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			}
		} else if (item.getType() == Material.IRON_PICKAXE) {
			g_ = pli.getAllGuns().get("grenade");
			if (g.pgunattributes.containsKey(p.getName())) {
				t = g.pgunattributes.get(p.getName()).containsKey(g_) ? g.pgunattributes.get(p.getName()).get(g_) : g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			} else {
				t = g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			}
		} else if (item.getType() == Material.IRON_SPADE) {
			g_ = pli.getAllGuns().get("pistol");
			if (g.pgunattributes.containsKey(p.getName())) {
				t = g.pgunattributes.get(p.getName()).containsKey(g_) ? g.pgunattributes.get(p.getName()).get(g_) : g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			} else {
				t = g.getPlayerGunAttributeLevels(this, p.getName(), g_);
			}
		}
		if (item.getDurability() > 240) {
			pwait.put(p.getName(), g_);
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					if (pwait.containsKey(p.getName())) {
						pwait.remove(p.getName());
						for (ItemStack i : p.getInventory().getContents()) {
							if (i != null && item != null) {
								if (i.getType() == item.getType()) {
									i.setDurability((short) 0);
								}
							}
						}
						p.updateInventory();
					}
				}
			}, 90L / (t[1] + 1));
		} else {
			item.setDurability((short) (item.getDurability() + 10 / (t[1] + 1)));
		}
		ret.put(g_, t);
		return ret;
	}

	/*
	 * public void shoot(ItemStack item, final Player p, int id, int durability, int durability_temp, int eggcount) { if (item.getDurability() <
	 * durability) { // 124 for (int i = 0; i < eggcount; i++) { p.launchProjectile(Egg.class); } item.setDurability((short) (item.getDurability() +
	 * durability_temp)); // 6 } else { if (!pusage.containsKey(p.getName())) { p.sendMessage(ChatColor.RED +
	 * "Please wait 3 seconds before using this gun again!"); Bukkit.getScheduler().runTaskLater(m, new Runnable() { public void run() {
	 * p.updateInventory(); p.getInventory().clear(); p.updateInventory(); Classes.getClass(m, p.getName()); if (pusage.containsKey(p.getName())) {
	 * pusage.remove(p.getName()); } } }, 20L * 3); pusage.put(p.getName(), id); } } }
	 */

	@EventHandler
	public void onEgg(PlayerEggThrowEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setHatching(false);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (pli.global_players.containsKey(p.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					if (event.getCause() == DamageCause.ENTITY_ATTACK) {
						p.setHealth(20D);
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (pli.global_players.containsKey(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Egg) {
			Egg egg = (Egg) event.getDamager();
			Player p = (Player) event.getEntity();
			Player attacker = (Player) egg.getShooter();
			if (pli.global_players.containsKey(p.getName()) && pli.global_players.containsKey(attacker.getName())) {
				IArena a = (IArena) pli.global_players.get(p.getName());
				if (a.getArenaState() == ArenaState.INGAME) {
					HashMap<Gun, int[]> t_ = this.evaluateGun(attacker.getItemInHand(), attacker);
					Gun g_ = (Gun) t_.keySet().toArray()[0];
					int[] t = t_.get(g_);
					if (g_ != null) {
						g_.onHit(p, t[3]);
					}
					lastdamager.put(p.getName(), attacker.getName());
				}
			}
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().equalsIgnoreCase("/upgrade") || event.getMessage().equalsIgnoreCase("/upgrades")) {
			if (pli.global_players.containsKey(event.getPlayer().getName())) {
				g.openGUI(event.getPlayer().getName());
				event.setCancelled(true);
				return;
			}
		}
	}

}

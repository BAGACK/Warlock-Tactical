package com.comze_instancelabs.mgwarlocktactical;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.MinigamesAPI;

public class IArena extends Arena {

	private BukkitTask timer;
	Main m = null;
	public int c = 30;
	BukkitTask tt;
	int currentingamecount;

	public IArena(Main m, String arena) {
		super(m, arena);
		this.m = m;
	}

	public void setRadius(int i) {
		this.c = i;
	}

	public void generateArena(Location start) {
		int x = start.getBlockX();
		int y = start.getBlockY();
		int z = start.getBlockZ();

		int c_2 = c * c;

		for (int x_ = -c; x_ <= c; x_++) {
			for (int z_ = -c; z_ <= c; z_++) {
				if ((x_ * x_) + (z_ * z_) <= c_2) {
					Block b = start.getWorld().getBlockAt(new Location(start.getWorld(), x - x_, y, z - z_));
					b.setType(Material.ICE);
				}
			}
		}

		// TODO generate lava floor
	}

	@Override
	public void spectate(String playername) {
		super.spectate(playername);
		// give killer a reward:
		if(m.lastdamager.containsKey(playername)){
			Player killer = Bukkit.getPlayer(m.lastdamager.get(playername));
			m.pli.getRewardsInstance().giveReward(playername);
			killer.sendMessage(MinigamesAPI.getAPI().pinstances.get(m).getMessagesConfig().you_got_a_kill.replaceAll("<player>", playername));
		}
	}

	@Override
	public void started() {
		for (String p_ : this.getAllPlayers()) {
			Player p = Bukkit.getPlayer(p_);
			p.sendMessage(ChatColor.RED + "The platform starts decreasing in 10 seconds!");
		}
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				timer = Bukkit.getScheduler().runTaskTimer(m, new Runnable() {
					public void run() {
						c--;
						if (c > 0) {
							removeCircle(c, Material.PACKED_ICE);
							Bukkit.getScheduler().runTaskLater(m, new Runnable() {
								public void run() {
									removeCircle(c, Material.AIR);
								}
							}, 16L); // 2L
						}
					}
				}, 0L, 20L); // 6L
			}
		}, 20L * 10);
	}

	public void removeCircle(int cr, Material mat) {
		int cradius_s = cr * cr;
		Location start = this.getSpawns().get(0);
		int x = start.getBlockX();
		int y = start.getBlockY();
		int z = start.getBlockZ();
		for (int x_ = -cr; x_ <= cr; x_++) {
			for (int z_ = -cr; z_ <= cr; z_++) {
				int t = (x_ * x_) + (z_ * z_);
				if (t >= cradius_s && t <= (cradius_s + 90)) {
					Block b = start.getWorld().getBlockAt(new Location(start.getWorld(), x - x_, y, z - z_));
					b.setType(mat);
				}
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		final IArena a = this;
		if (timer != null) {
			timer.cancel();
		}
		Bukkit.getScheduler().runTaskLater(m, new Runnable() {
			public void run() {
				c = Main.global_arenas_size;
				a.generateArena(a.getSpawns().get(0));
			}
		}, 10L);
	}

}

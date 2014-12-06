package com.comze_instancelabs.mgwarlocktactical;

import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.Arena;
import com.comze_instancelabs.minigamesapi.Rewards;

public class IRewards extends Rewards {

	public IRewards(JavaPlugin plugin) {
		super(plugin);
	}

	@Override
	public void giveWinReward(String p, Arena a){
		// 
	}
}

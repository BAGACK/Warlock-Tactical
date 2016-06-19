package com.comze_instancelabs.mgwarlocktactical;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.minigamesapi.ArenaListener;
import com.comze_instancelabs.minigamesapi.PluginInstance;

public class IArenaListener extends ArenaListener {

	public IArenaListener(JavaPlugin plugin, PluginInstance pinstance) {
		super(plugin, pinstance, "warlocktactic", new ArrayList<String>(Arrays.asList("/wlt")));
	}

}

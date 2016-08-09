package com.comze_instancelabs.mgwarlocktactical;

import com.comze_instancelabs.minigamesapi.MinigamesAPI;
import com.comze_instancelabs.minigamesapi.config.ClassesConfig;

public class IClassesConfig extends ClassesConfig {

	public IClassesConfig(Main m){
		super(m, true);
		this.getConfig().options().header("Used for saving classes. Default class:");
		
		// default (stone)
    	this.getConfig().addDefault("config.kits.default.name", "default");
    	this.getConfig().addDefault("config.kits.default.items", "385*1");
    	this.getConfig().addDefault("config.kits.default.lore", "Stone");
    	this.getConfig().addDefault("config.kits.default.requires_money", false);
    	this.getConfig().addDefault("config.kits.default.requires_permission", false);
    	this.getConfig().addDefault("config.kits.default.money_amount", 100);
    	this.getConfig().addDefault("config.kits.default.permission_node", MinigamesAPI.getAPI().getPermissionKitPrefix() + ".default");
    	
    	// iron
    	this.getConfig().addDefault("config.kits.iron.name", "iron");
    	this.getConfig().addDefault("config.kits.iron.items", "368*1;385*2");
    	this.getConfig().addDefault("config.kits.iron.lore", "Iron");
    	this.getConfig().addDefault("config.kits.iron.requires_money", false);
    	this.getConfig().addDefault("config.kits.iron.requires_permission", false);
    	this.getConfig().addDefault("config.kits.iron.money_amount", 100);
    	this.getConfig().addDefault("config.kits.iron.permission_node", MinigamesAPI.getAPI().getPermissionKitPrefix() + ".iron");
    	
    	// diamond
    	this.getConfig().addDefault("config.kits.diamond.name", "diamond");
    	this.getConfig().addDefault("config.kits.diamond.items", "368*2;385*3");
    	this.getConfig().addDefault("config.kits.diamond.lore", "Diamond");
    	this.getConfig().addDefault("config.kits.diamond.requires_money", false);
    	this.getConfig().addDefault("config.kits.diamond.requires_permission", false);
    	this.getConfig().addDefault("config.kits.diamond.money_amount", 100);
    	this.getConfig().addDefault("config.kits.diamond.permission_node", MinigamesAPI.getAPI().getPermissionKitPrefix() + ".diamond");

    	this.getConfig().options().copyDefaults(true);
    	this.saveConfig();
	}
	
}

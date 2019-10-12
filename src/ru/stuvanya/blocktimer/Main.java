package ru.stuvanya.blocktimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	public static Main plugin;
	
	HashMap<Material, Long> placeBlocksDurations = new HashMap<Material, Long>();
	HashMap<Material, Long> breakBlocksDurations = new HashMap<Material, Long>();
	List<World> worlds = new ArrayList<World>();

	public void onEnable() {
		plugin = this;
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		this.saveDefaultConfig();
		
		for (String w : getConfig().getStringList("worlds")) {
			World world = Bukkit.getWorld(w);
			if (world != null)
				worlds.add(world);
		}
		
		for (String s : getConfig().getConfigurationSection("break").getKeys(true)) {
			Long time = getConfig().getLong("break." + s);
			
			Material material;
			if (Material.matchMaterial(s) != null) {
				material = Material.matchMaterial(s);
            } else if (Material.matchMaterial(s, true) != null) {
            	material = Material.matchMaterial(s, true);
            } else {
                System.out.println("Wrong id: " + s);
                continue;
            }
			breakBlocksDurations.put(material, time);
		}
		
		for (String s : getConfig().getConfigurationSection("place").getKeys(true)) {
			Long time = getConfig().getLong("place." + s);
			
			Material material;
			if (Material.matchMaterial(s) != null) {
				material = Material.matchMaterial(s);
            } else if (Material.matchMaterial(s, true) != null) {
            	material = Material.matchMaterial(s, true);
            } else {
                System.out.println("Wrong id: " + s);
                continue;
            }
			placeBlocksDurations.put(material, time);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		Block block = e.getBlock();
		if (worlds.contains(block.getWorld())) {
			Material material = block.getType();
			BlockState bs = block.getState();
			
			if (breakBlocksDurations.containsKey(material)) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						block.setType(material);
						block.setBlockData(bs.getBlockData());
					}
					
				}, breakBlocksDurations.get(material));
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Block block = e.getBlockPlaced();
		if (worlds.contains(block.getWorld())) {
			BlockState bs = e.getBlockReplacedState();
			
			if (placeBlocksDurations.containsKey(block.getType())) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						block.setType(bs.getBlock().getType());
						block.setBlockData(bs.getBlockData());
					}
					
				}, placeBlocksDurations.get(block.getType()));
			}
		}
	}
}

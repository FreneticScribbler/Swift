package com.aronajones.swift;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "swift", name = "Swift", version = "0.0.1")
public class Swift {
	public static int chunkUpdates, ticksExisted;
	public static int[] fpsValues;
	public static String[] commands;
	public static String[] warnings;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		// TODO Sanitisation.
		chunkUpdates = config.getInt("chunkUpdates", Configuration.CATEGORY_GENERAL, 15, 0, Integer.MAX_VALUE,
				"Commands will not run if chunk updates are greater than or equal to this value.");
		ticksExisted = config.getInt("ticksExisted", Configuration.CATEGORY_GENERAL, 20000, 0, Integer.MAX_VALUE,
				"Commands will not run if the player has been in the world for less than this value, given in ticks.");
		fpsValues = config
				.get(Configuration.CATEGORY_GENERAL, "fpsValues", new int[] {30, 60},
						"Values at which to run commands/give warnings. If the string is empty it will be safely ignored.")
				.getIntList();
		commands = config.getStringList("commandsAtFPSValues", Configuration.CATEGORY_GENERAL, new String[] {"", ""},
				"Must be in the same order as FPS values.");
		warnings = config.getStringList("warningsAtFPSValues", Configuration.CATEGORY_GENERAL,
				new String[] {"Under 30.", "Under 60."}, "Must be in the same order as FPS values.");
		if(config.hasChanged())
			config.save();

		FMLCommonHandler.instance().bus().register(new SwiftEventHandler());
	}
}

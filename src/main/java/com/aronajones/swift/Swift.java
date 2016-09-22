package com.aronajones.swift;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "swift", name = "Swift", version = "0.0.1", canBeDeactivated = true)
public class Swift {
	public static int chunkUpdates, ticksExisted, ticksBetweenRun;
	public static int[] lowerFPSValues, upperFPSValues;
	public static String[] lowerCommands, upperCommands;
	public static String[] lowerWarnings, upperWarnings;
	public static int cooldownTicks;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		chunkUpdates = config.getInt("chunkUpdates", Configuration.CATEGORY_GENERAL, 15, 0, Integer.MAX_VALUE,
				"Commands will not run if chunk updates are greater than or equal to this value.");
		ticksExisted = config.getInt("ticksExisted", Configuration.CATEGORY_GENERAL, 20000, 0, Integer.MAX_VALUE,
				"Commands will not run if the player has been in the world for less than this value, given in ticks.");
		ticksBetweenRun = config.getInt("ticksBetweenRun", Configuration.CATEGORY_GENERAL, 60, 0, Integer.MAX_VALUE,
				"The frequency with which the check runs.");
		cooldownTicks = config.getInt("cooldownTicks", Configuration.CATEGORY_GENERAL, 120, 0, Integer.MAX_VALUE,
				"Cooldown between running of commands/warnings.");
		lowerFPSValues = config
				.get(Configuration.CATEGORY_GENERAL, "lowerFPSValues", new int[] {30, 60},
						"Values at which to run commands/give warnings. If the string is empty it will be safely ignored.")
				.getIntList();
		lowerCommands = config.getStringList("commandsAtLowerFPSValues", Configuration.CATEGORY_GENERAL,
				new String[] {"", ""}, "Must be in the same order as FPS values.");
		lowerWarnings = config.getStringList("warningsAtLowerFPSValues", Configuration.CATEGORY_GENERAL,
				new String[] {"Under 30.", "Under 60."}, "Must be in the same order as FPS values.");
		upperFPSValues = config
				.get(Configuration.CATEGORY_GENERAL, "upperFPSValues", new int[] {10},
						"Values at which to run commands/give warnings. If the string is empty it will be safely ignored.")
				.getIntList();
		upperCommands = config.getStringList("commandsAtUpperFPSValues", Configuration.CATEGORY_GENERAL,
				new String[] {""}, "Must be in the same order as FPS values.");
		upperWarnings = config.getStringList("warningsAtUpperFPSValues", Configuration.CATEGORY_GENERAL,
				new String[] {"Over 100."}, "Must be in the same order as FPS values.");
		if(config.hasChanged())
			config.save();

		assert lowerFPSValues.length == lowerCommands.length;
		assert lowerCommands.length == lowerWarnings.length;
		assert upperFPSValues.length == upperCommands.length;
		assert upperCommands.length == upperWarnings.length;

		FMLCommonHandler.instance().bus().register(new SwiftEventHandler());
	}
}

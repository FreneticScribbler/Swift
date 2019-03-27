package com.aronajones.swift;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.logging.log4j.Logger;

import com.aronajones.swift.profiles.ProfileCommand;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.opengl.Display;

@Mod(modid = Swift.MODID, name = Swift.NAME, version = Swift.VERSION, canBeDeactivated = false)
public class Swift {

	public static final String MODID = "swift";
	public static final String NAME = "Swift";
	public static final String VERSION = "0.0.4.2";

	public static int chunkUpdates, ticksExisted, ticksBetweenRun;
	public static int cooldownTicks;
	public static int refreshRate;
	public static Value[] lowers = new Value[0];
	public static Value[] uppers = new Value[0];

	public static Logger logger;
	public static File profileConfig = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
			logger.warn("WARNING! You're loading a CLIENT only mod on a server!");
		profileConfig = new File(event.getModConfigurationDirectory() + File.separator + MODID + "_profiles.cfg");

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		chunkUpdates = config.getInt("chunkUpdates", Configuration.CATEGORY_GENERAL, 15, 0, Integer.MAX_VALUE,
				"Commands will not run if chunk updates are greater than or equal to this value.");
		ticksExisted = config.getInt("ticksExisted", Configuration.CATEGORY_GENERAL, 1200, 0, Integer.MAX_VALUE,
				"Commands will not run if the player has been in the world for less than this value, given in ticks.");
		ticksBetweenRun = config.getInt("ticksBetweenRun", Configuration.CATEGORY_GENERAL, 60, 0, Integer.MAX_VALUE,
				"The frequency with which the check runs.");
		cooldownTicks = config.getInt("cooldownTicks", Configuration.CATEGORY_GENERAL, 120, 0, Integer.MAX_VALUE,
				"Cooldown between running of commands/warnings.");
		int[] lowerFPSValues = config
				.get(Configuration.CATEGORY_GENERAL, "lowerFPSValues", new int[]{30, 60, 25},
						"Values at which to run commands/give warnings. If the string is empty it will be safely ignored.")
				.getIntList();
		String[] lowerCommands = config.getStringList("commandsAtLowerFPSValues", Configuration.CATEGORY_GENERAL,
				new String[]{"", "", ""}, "Must be in the same order as FPS values.");
		String[] lowerWarnings = config.getStringList("warningsAtLowerFPSValues", Configuration.CATEGORY_GENERAL,
				new String[]{"Under 30.", "Under 60.", "Under 25"}, "Must be in the same order as FPS values.");
		int[] upperFPSValues = config
				.get(Configuration.CATEGORY_GENERAL, "upperFPSValues", new int[]{40, 100, 120},
						"Values at which to run commands/give warnings. If the string is empty it will be safely ignored.")
				.getIntList();
		String[] upperCommands = config.getStringList("commandsAtUpperFPSValues", Configuration.CATEGORY_GENERAL,
				new String[]{"", "", ""}, "Must be in the same order as FPS values.");
		String[] upperWarnings = config.getStringList("warningsAtUpperFPSValues", Configuration.CATEGORY_GENERAL,
				new String[]{"Over 40.", "Over 100.", "Over 120."}, "Must be in the same order as FPS values.");
		boolean[] upperFPSValuesOverrides = config
				.get(Configuration.CATEGORY_GENERAL, "upperFPSValuesOverrides", new boolean[]{false, false, false},
						"Whether or not to ignore the client's refresh rate when running a corresponding upperFPSValue. Set to true to ignore, that is, force the execution of any commands and warnings associated with the corresponding upperFPSValue, regardless of the client's refresh rate.")
				.getBooleanList();
		if(config.hasChanged())
			config.save();

		// TODO Graceful errors
		assert lowerFPSValues.length == lowerCommands.length;
		assert lowerCommands.length == lowerWarnings.length;
		assert upperFPSValues.length == upperCommands.length;
		assert upperCommands.length == upperWarnings.length;
		assert upperFPSValuesOverrides.length == upperFPSValues.length;

		ArrayList<Value> lowers = new ArrayList<Value>();
		ArrayList<Value> uppers = new ArrayList<Value>();

		for(int i = 0; i < lowerFPSValues.length; i++)
			lowers.add(new Value(lowerFPSValues[i], lowerCommands[i], lowerWarnings[i], false));

		for(int i = 0; i < upperFPSValues.length; i++)
			uppers.add(new Value(upperFPSValues[i], upperCommands[i], upperWarnings[i], upperFPSValuesOverrides[i]));

		lowers.sort(new ComparatorValue());
		uppers.sort(new ComparatorValue());

		Swift.lowers = lowers.toArray(Swift.lowers);
		Swift.uppers = uppers.toArray(Swift.uppers);

		FMLCommonHandler.instance().bus().register(new SwiftEventHandler());
		ClientCommandHandler.instance.registerCommand(new ProfileCommand());

		// Get the refresh rate of the player's screen (only checked at launch as LWJGL doesn't update this value after the game is launched)
		if(Display.isFullscreen()) {
			refreshRate = Display.getDisplayMode().getFrequency(); // get the fullscreen mode refresh rate
		} else {
			refreshRate = Display.getDesktopDisplayMode().getFrequency(); // get the desktop mode refresh rate
		}
	}

	class Value {
		public int fps;
		public String command;
		public String warning;
		public boolean overrides;

		public Value(int fps, String command, String warning, boolean overrides) {
			this.fps = fps;
			this.command = command;
			this.warning = warning;
			this.overrides = overrides;
		}
	}

	class ComparatorValue implements Comparator<Value> {

		@Override
		public int compare(Value o1, Value o2) {
			return Integer.compare(o1.fps, o2.fps);
		}

	}
}

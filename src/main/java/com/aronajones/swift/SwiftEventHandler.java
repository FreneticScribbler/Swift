package com.aronajones.swift;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeHooks;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

public class SwiftEventHandler {

	// TODO Unification
	private int ticks = 0;
	private int cooldown = 0;

	private int lastLoadedProfile = -1;
	private int secondLastLoadedProfile = -1;
	private int previousFramerateCap = 999;
	private boolean idleModeRunning = false;

	private void delaySwift(int delayTicks) {
		if(cooldown < (Swift.cooldownTicks + delayTicks)) { // don't add any extra ticks if the cooldown is already sufficient
			cooldown = cooldown + delayTicks; // add 90 ticks to the cooldown to prevent Swift triggering during something important (e.g. screenshot or clock speed ramp up)
		}
	}

	@SubscribeEvent @SideOnly(Side.CLIENT)
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.side.isClient() && Swift.isEnabled) {
			//Swift.logger.info("cooldown: " + cooldown);
			//Swift.logger.info("ticks: " + ticks);

			boolean inFocus = (Display.isActive() || Minecraft.getMinecraft().inGameHasFocus); // set inFocus to true if either the window is in focus or the game is not paused in a multiplayer server

			if(Swift.idleModeEnabled) { // if idle mode is enabled in the config
				if(inFocus && idleModeRunning) { // if the window is currently in focus and idle mode is already running
					Minecraft.getMinecraft().gameSettings.limitFramerate = previousFramerateCap; // undo idle mode
					idleModeRunning = false; // disable idle mode
					delaySwift(60); // add 60 ticks to the cooldown (if necessary) to help prevent Swift from triggering during GPU clock speed ramp up
					return; // done
				} else if(!inFocus && !idleModeRunning){ // if the window isn't in focus and idle mode isn't already running
					idleModeRunning = true; // enable idle mode
					previousFramerateCap = Minecraft.getMinecraft().gameSettings.limitFramerate; // store the framerate before running idle mode so it can be undone later
					Minecraft.getMinecraft().gameSettings.limitFramerate = Swift.idleModeFramerate; // set the idle mode framerate
					System.gc(); // use this opportunity to advise the JVM to run the garbage collector
					return; // done
				} else if(idleModeRunning) {
					return; // don't run other Swift triggers in idle mode
				}
			}

			try {
				//if(Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindScreenshot.getKeyCode())) { // if the screenshot key is being pressed
				if(Minecraft.getMinecraft().gameSettings.keyBindScreenshot.getIsKeyPressed()) { // if the screenshot key is being pressed
					//Swift.logger.info("screenshot key pressed");
					delaySwift(90); // add 90 ticks to the cooldown (if necessary) to help prevent Swift from triggering while taking a screenshot
				} else {
					//Swift.logger.info("screenshot key NOT pressed");
				}
			} catch (Exception e) {
				//Swift.logger.warn("Error checking the screenshot key state.");
			}

			if(cooldown > 0)
				// Deduct the cooldown on every tick
				cooldown--;

			// If it's past the cooldown time and the tick interval time
			if(ticks >= Swift.ticksBetweenRun && cooldown < 1) {
				// reset the ticks interval
				ticks = 0;

				// for running any commands associated to profiles later on
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;

				// grab all the details from the F3 debug screen
				String[] debug = Minecraft.getMinecraft().debug.split(" ");

				int fps = Integer.parseInt(debug[0]); // grab the current FPS as shown on the debug screen
				int chunkUpdates = Integer.parseInt(debug[2]); // do the same for chunk updates

				// Optifine tends to attempt to keep the chunk updates to at least the framerate.
				if(FMLClientHandler.instance().hasOptifine() && chunkUpdates > fps) {
					chunkUpdates = chunkUpdates - fps; // Take this into account for consistent behaviour.
				}

				// Don't run Swift when chunks are still being loaded and/or the player has recently entered the world
				if(chunkUpdates >= Swift.chunkUpdates || player.ticksExisted < Swift.ticksExisted)
					return;

				// for each entry in the "lowers" profile entries config
				for(int i = 0; i < Swift.lowers.length; i++) {

					// if the framerate is below the "lowers" profile entry
					if(fps < Swift.lowers[i].fps) {

						// if the profile we're about to load isn't already loaded
						if(i != lastLoadedProfile) {

							// if a command for the profile has been specified in the config, run it
							if(!Swift.lowers[i].command.isEmpty()) {
								ClientCommandHandler.instance.executeCommand(player, Swift.lowers[i].command);
							}

							// if a warning for the profile has been specified in the config, print it out to the chat
							if(!Swift.lowers[i].warning.isEmpty()) {
								player.addChatMessage(new ChatComponentText(Swift.lowers[i].warning));
							}
						}

						// reset the cooldown timer to the value specified in the config
						cooldown = Swift.cooldownTicks;

						// set the secondLastLoadedProfile to lastLoadedProfile
						secondLastLoadedProfile = lastLoadedProfile;

						// set the lastLoadedProfile to the current one
						lastLoadedProfile = i;

						// done
						break;
					}
				}

				// for each entry in the "uppers" profile entries config
				for(int i = 0; i < Swift.uppers.length; i++) {
					//player.addChatMessage(new ChatComponentText("Swift debug: current uppers: " + Swift.uppers[i].warning));
					//player.addChatMessage(new ChatComponentText("Swift debug: Uppers length: " + Swift.uppers.length));
					//player.addChatMessage(new ChatComponentText("Swift debug: Math.ceil(refreshRate * 0.8) = " + Math.ceil(Swift.refreshRate * 0.8)));

                    // refreshRate * 0.6:
					// 144hz = 86.4
					// 120hz = 72
					// 75hz = 45
					// 60hz = 36

                    // refreshRate * 0.7:
					// 144hz = 100.8
					// 120hz = 84
					// 75hz = 52.5
					// 60hz = 42

					// refreshRate * 0.8:
					// 144hz = 115.2
					// 120hz = 96
					// 75hz = 60
					// 60hz = 48

					// if the framerate is above the "uppers" profile entry and above (or equal to) 80% of the monitor's refresh rate. Or if it's above the "uppers" and it's an override.
					if((fps > Swift.uppers[i].fps && Swift.uppers[i].fps >= Math.ceil(Swift.refreshRate * 0.8)) || (fps > Swift.uppers[i].fps && Swift.uppers[i].overrides)) {

						// if the profile we're about to load isn't already loaded
						if(i != lastLoadedProfile) {

							// if a command for the profile has been specified in the config, run it
							if(!Swift.uppers[i].command.isEmpty()) {
								ClientCommandHandler.instance.executeCommand(player, Swift.uppers[i].command);
							}

							// if a warning for the profile has been specified in the config, print it out to the chat
							if(!Swift.uppers[i].warning.isEmpty()) {
								player.addChatMessage(new ChatComponentText(Swift.uppers[i].warning));
							}
						}

						// reset the cooldown timer to the value specified in the config
						cooldown = Swift.cooldownTicks;

						// set the secondLastLoadedProfile to lastLoadedProfile
						secondLastLoadedProfile = lastLoadedProfile;

						// set the lastLoadedProfile to the current one
						lastLoadedProfile = i;

						// done
						break;
					}
				}
			}
			else {
				// if we're still cooling down, increment the tick timer
				ticks++;
			}
		}
	}
}

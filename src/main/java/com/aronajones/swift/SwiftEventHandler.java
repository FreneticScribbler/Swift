package com.aronajones.swift;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;

public class SwiftEventHandler {

	// TODO Unification
	int ticks = 0;
	int cooldown = 0;
	int lastLoadedProfile = -1;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.side.isClient()) {
			if(cooldown > 0)
				cooldown--;

			if(ticks >= Swift.ticksBetweenRun && cooldown == 0) {
				ticks = 0;
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				String[] debug = Minecraft.getMinecraft().debug.split(" ");
				int fps = Integer.parseInt(debug[0]);
				int chunkUpdates = Integer.parseInt(debug[2]);
				if(FMLClientHandler.instance().hasOptifine()) {
					chunkUpdates = chunkUpdates - fps;
				}
				if(chunkUpdates >= Swift.chunkUpdates || player.ticksExisted < Swift.ticksExisted)
					return;
				for(int i = 0; i < Swift.lowers.length; i++) {
					if(fps < Swift.lowers[i].fps) {
						if(i != lastLoadedProfile) {
							if(!Swift.lowers[i].command.isEmpty()) {
								ClientCommandHandler.instance.executeCommand(player, Swift.lowers[i].command);
							}
							if(!Swift.lowers[i].warning.isEmpty()) {
								player.addChatMessage(new ChatComponentText(Swift.lowers[i].warning));
							}
						}
						cooldown = Swift.cooldownTicks;
						lastLoadedProfile = i;
						break;
					}
				}
				for(int i = 0; i < Swift.uppers.length; i++) {
					if(fps > Swift.uppers[i].fps) {
						if(i != lastLoadedProfile) {
							if(!Swift.uppers[i].command.isEmpty()) {
								ClientCommandHandler.instance.executeCommand(player, Swift.uppers[i].command);
							}
							if(!Swift.uppers[i].warning.isEmpty()) {
								player.addChatMessage(new ChatComponentText(Swift.uppers[i].warning));
							}
						}
						cooldown = Swift.cooldownTicks;
						lastLoadedProfile = i;
						break;
					}
				}
				return;
			}
			else {
				ticks++;
				return;
			}
		}
	}
}

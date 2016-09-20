package com.aronajones.swift;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.ChatComponentText;

public class SwiftEventHandler {

	int ticks = 0;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.side.isClient()) {
			if(ticks >= Swift.ticksBetweenRun) {
				ticks = 0;
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				String[] debug = Minecraft.getMinecraft().debug.split(" ");
				int fps = Integer.parseInt(debug[0]);
				int chunkUpdates = Integer.parseInt(debug[2]);
				if(chunkUpdates >= Swift.chunkUpdates || player.ticksExisted < Swift.ticksExisted)
					return;
				for(int i = 0; i < Swift.lowerFPSValues.length; i++) {
					if(fps < Swift.lowerFPSValues[i]) {
						if(!Swift.lowerCommands[i].isEmpty())
							Minecraft.getMinecraft().thePlayer.sendChatMessage(Swift.lowerCommands[i]);
						if(!Swift.lowerWarnings[i].isEmpty())
							player.addChatMessage(new ChatComponentText(Swift.lowerWarnings[i]));
					}
				}
				for(int i = 0; i < Swift.upperFPSValues.length; i++) {
					if(fps > Swift.upperFPSValues[i]) {
						if(!Swift.upperCommands[i].isEmpty())
							Minecraft.getMinecraft().thePlayer.sendChatMessage(Swift.upperCommands[i]);
						if(!Swift.upperWarnings[i].isEmpty())
							player.addChatMessage(new ChatComponentText(Swift.upperWarnings[i]));
					}
				}
				return;
			}
			else {
				FMLLog.warning("" + ticks);
				ticks++;
				return;
			}
		}
	}
}

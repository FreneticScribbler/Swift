package com.aronajones.swift;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.util.ChatComponentText;

public class SwiftEventHandler {

	// long lastUpdateTime = Minecraft.getSystemTime();

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		if(event.side.isClient()) {
			// TODO Delay
			// if(Minecraft.getSystemTime() == lastUpdateTime + 1000L) {
			EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
			String[] debug = Minecraft.getMinecraft().debug.split(" ");
			int fps = Integer.parseInt(debug[0]);
			int chunkUpdates = Integer.parseInt(debug[2]);
			if(chunkUpdates >= Swift.chunkUpdates || player.ticksExisted < Swift.ticksExisted)
				return;
			for(int i = 0; i < Swift.fpsValues.length; i++) {
				if(fps < Swift.fpsValues[i]) {
					if(!Swift.commands[i].isEmpty())
						Minecraft.getMinecraft().thePlayer.sendChatMessage(Swift.commands[i]);
					if(!Swift.warnings[i].isEmpty())
						player.addChatMessage(new ChatComponentText(Swift.warnings[i]));
				}
			}
			// lastUpdateTime += 1000L;
			// }
		}
	}
}

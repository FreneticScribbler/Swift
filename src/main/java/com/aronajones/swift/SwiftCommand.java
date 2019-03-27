package com.aronajones.swift;

import com.aronajones.swift.profiles.Profile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;
import java.util.Map;

public class SwiftCommand extends CommandBase {
	public static final String NAME = "swift";

	public static final String DISABLE = "disable";
	public static final String ENABLE = "enable";

	public static final String USAGE = "commands.swift.usage";
	public static final String DISABLED = "commands.swift.disabled";
	public static final String ENABLED = "commands.swift.enabled";

	private Map<String, Profile> profiles;

	@Override
	public String getCommandName() {
		return NAME;
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return USAGE;
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender commandSender, String[] args) {
		if(args.length == 1)
			return getListOfStringsMatchingLastWord(args, ENABLE, DISABLE);

		return null;
	}

	@Override
	public void processCommand(ICommandSender commandSender, String[] args) {
		if(args.length > 0)
			if(args[0].equalsIgnoreCase(DISABLE)) {
				Swift.isEnabled = false;
				commandSender.addChatMessage(new ChatComponentTranslation(DISABLED));
			}
			else if(args[0].equalsIgnoreCase(ENABLE)) {
				Swift.isEnabled = true;
				commandSender.addChatMessage(new ChatComponentTranslation(ENABLED));
			}
			else {
				throw new WrongUsageException(getCommandUsage(commandSender));
			}
		else {
			throw new WrongUsageException(getCommandUsage(commandSender));
		}
	}
}

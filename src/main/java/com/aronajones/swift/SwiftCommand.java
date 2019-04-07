package com.aronajones.swift;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;

import com.aronajones.swift.Swift;

import java.util.List;

public class SwiftCommand extends CommandBase {
	private static final String NAME = "swift";

	private static final String DISABLE = "disable";
	private static final String ENABLE = "enable";

	private static final String USAGE = "commands.swift.usage";
	private static final String DISABLED = "commands.swift.disabled";
	private static final String ENABLED = "commands.swift.enabled";

	public SwiftCommand() {
		//this.gson = new GsonBuilder().setPrettyPrinting().create();
		//this.profiles = readFile(Swift.profileConfig);
	}

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
				return;
			}
			else if(args[0].equalsIgnoreCase(ENABLE)) {
				Swift.isEnabled = true;
				commandSender.addChatMessage(new ChatComponentTranslation(ENABLED));
				return;
			}
			else {
				throw new WrongUsageException(getCommandUsage(commandSender));
			}
		else {
			throw new WrongUsageException(getCommandUsage(commandSender));
		}
	}
}

package fr.partipirate.discord.bots.congressus.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public interface ICommand {
	String getKeyWord();
	boolean canDoCommand(User user, Guild guild);
	void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts);
	String getCommandHelp();
}
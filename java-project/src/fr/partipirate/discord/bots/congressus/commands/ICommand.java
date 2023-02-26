package fr.partipirate.discord.bots.congressus.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public interface ICommand {
	String getKeyWord();
	boolean canDoCommand(User user, Guild guild);
	void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts);
	String getCommandHelp();
}
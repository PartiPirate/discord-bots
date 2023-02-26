package fr.partipirate.discord.bots.congressus.commands.dj;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class SkipCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public SkipCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "skip";
	}

	@Override
	public boolean canDoCommand(User user, Guild guild) {
		return isDJ(getMember(user, guild));
	}

	@Override
	public void doCommand(User user, MessageChannel channel, Guild guild, String[] commandParts) {
		if (channel instanceof PrivateChannel) {
        	channel.sendMessage("*Commande à faire publiquement*").complete();
		}
		else if (!isDJ(getMember(user, guild))) {
        	channel.sendMessage("*Vous n'avez pas les droits suffisants*").complete();
		}
		else if (commandParts.length > 1) {
			int trackNumber = Integer.parseInt(commandParts[1]);
			
			if (trackNumber == 1) {
				bot.skipTrack((TextChannel) channel);
			}
			else {
				bot.skipTrack((TextChannel) channel, trackNumber);
			}
		}
		else {
			bot.skipTrack((TextChannel) channel);
		}
	}
	
	@Override
	public String getCommandHelp() {
		return "Passe à la piste suivante (ou supprime la *n*ième piste si un paramètre est fourni).";
	}
}

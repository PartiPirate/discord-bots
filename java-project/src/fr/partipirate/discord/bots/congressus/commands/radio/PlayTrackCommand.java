package fr.partipirate.discord.bots.congressus.commands.radio;

import java.util.Iterator;
import java.util.Map;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.GuildMusicManager;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import fr.partipirate.discord.bots.congressus.commands.dj.ADJCommand;
import fr.partipirate.discord.bots.congressus.listeners.RadioHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class PlayTrackCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public PlayTrackCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "playtrack";
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
		else if (commandParts.length < 2) {
        	channel.sendMessage("*Il faut un identifiant de jingle*").complete();
		}
		else {
			bot.emptyTracks((TextChannel) channel);
			bot.skipTrack((TextChannel) channel);
			
			Map<Long, GuildMusicManager> managers = bot.getMusicManagers();

			for (Iterator<GuildMusicManager> iterator = managers.values().iterator(); iterator.hasNext();) {
				GuildMusicManager manager = iterator.next();

				RadioHandler.getInstance().getTrack(manager, commandParts[1]);
			}
		}
	}
	
	@Override
	public String getCommandHelp() {
		return "Joue une track précise [id]";
	}
}

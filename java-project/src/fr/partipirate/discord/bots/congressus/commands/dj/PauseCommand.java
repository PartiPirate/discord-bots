package fr.partipirate.discord.bots.congressus.commands.dj;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class PauseCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public PauseCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "pause";
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
		else {
			getPlayer(guild).setPaused(true);
		}
	}
	
	@Override
	public String getCommandHelp() {
		return "Met en pause.";
	}

	private AudioPlayer getPlayer(Guild guild) {
		return bot.getGuildAudioPlayer(guild).getPlayer();
	}
}
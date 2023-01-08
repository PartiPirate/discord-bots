package fr.partipirate.discord.bots.congressus.commands.dj;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import fr.partipirate.discord.bots.congressus.CongressusBot;
import fr.partipirate.discord.bots.congressus.commands.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class SeekCommand extends ADJCommand implements ICommand {

	private CongressusBot bot;

	public SeekCommand(CongressusBot bot) {
		this.bot = bot;
	}
	
	@Override
	public String getKeyWord() {
		return "seek";
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
			getPlayer(guild).getPlayingTrack().setPosition(Integer.parseInt(commandParts[1]) * 1000L);
		}
		else {
			channel.sendMessage("Position (pour reprendre c'est plus facile) : " + (getPlayer(guild).getPlayingTrack().getPosition() / 1000L)).complete();
		}
	}

	@Override
	public String getCommandHelp() {
		return "Met la piste courante à la position indiquée (en seconde).";
	}

	private AudioPlayer getPlayer(Guild guild) {
		return bot.getGuildAudioPlayer(guild).getPlayer();
	}
}